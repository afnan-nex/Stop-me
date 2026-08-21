package com.afnan.stopme.service.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import android.util.Log
import com.afnan.stopme.core.common.utils.DAILY_LIMIT_MILLIS
import com.afnan.stopme.core.common.utils.InstalledAppsHelper
import com.afnan.stopme.core.common.utils.WARNING_THRESHOLD_MILLIS
import com.afnan.stopme.core.common.utils.toCountdownString
import com.afnan.stopme.core.common.utils.toMinuteSecondString
import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import com.afnan.stopme.domain.repository.SettingsRepository
import com.afnan.stopme.domain.repository.UsageRepository
import com.afnan.stopme.domain.usecase.UnlockAppUseCase
import com.afnan.stopme.service.notification.UsageNotificationManager
import com.afnan.stopme.service.overlay.OverlayManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StopMeTracker"

/**
 * Core engine for detecting foreground application changes, maintaining monotonic per-app timers,
 * persisting usage records, updating silent persistent notifications, and driving blocking/warning overlays.
 */
@Singleton
class ForegroundAppTracker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val protectedAppRepo: ProtectedAppRepository,
    private val usageRepo: UsageRepository,
    private val settingsRepo: SettingsRepository,
    private val unlockAppUseCase: UnlockAppUseCase,
    private val notificationManager: UsageNotificationManager
) {
    private val trackerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val transitionMutex = Mutex()

    @Volatile private var protectedPackages: Set<String> = emptySet()
    @Volatile private var unlockChallengeEnabled: Boolean = true

    // Overlay manager set by the AccessibilityService
    private var overlayManager: OverlayManager? = null
    private var sendHomeAction: (() -> Unit)? = null

    // Timing state (Main thread)
    private var currentForegroundPackage: String? = null
    private var activeTrackingPackage: String? = null
    private var sessionStartElapsed: Long = 0L
    private var sessionBaseMillis: Long = 0L
    private var tickerJob: Job? = null
    private var warningShown: Boolean = false
    private var currentDate: String = todayLocalDate().toDateString()
    private var isScreenInteractive: Boolean = true

    // Receiver for Screen On/Off & Device Lock
    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    Log.d(TAG, "Screen OFF detected -> pausing active tracking")
                    isScreenInteractive = false
                    trackerScope.launch {
                        transitionMutex.withLock {
                            pauseTrackingSessionInternal()
                        }
                    }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    Log.d(TAG, "Screen ON / User present detected")
                    isScreenInteractive = true
                    currentForegroundPackage?.let { pkg ->
                        trackerScope.launch {
                            handlePackageChange(pkg)
                        }
                    }
                }
            }
        }
    }

    fun attach(overlayManager: OverlayManager, sendHome: () -> Unit) {
        this.overlayManager = overlayManager
        this.sendHomeAction = sendHome
        observeProtectedPackages()
        observeSettings()
        registerScreenReceiver()
    }

    fun detach() {
        trackerScope.launch {
            transitionMutex.withLock {
                pauseTrackingSessionInternal()
            }
        }
        unregisterScreenReceiver()
        trackerScope.cancel()
    }

    private fun registerScreenReceiver() {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        context.registerReceiver(screenReceiver, filter)
    }

    private fun unregisterScreenReceiver() {
        try {
            context.unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Called by AccessibilityService when a window/package transition occurs.
     */
    fun onPackageDetected(packageName: String) {
        if (!isScreenInteractive) return

        // Check for date rollover
        val today = todayLocalDate().toDateString()
        if (today != currentDate) {
            currentDate = today
            trackerScope.launch {
                persistCurrentSession()
            }
        }

        if (packageName != currentForegroundPackage) {
            Log.d(TAG, "Foreground package changed: $packageName")
            currentForegroundPackage = packageName
            trackerScope.launch {
                handlePackageChange(packageName)
            }
        }
    }

    /**
     * Resets in-memory tracking session when an app's countdown is reset by user.
     */
    fun onResetUsage(packageName: String) {
        trackerScope.launch {
            transitionMutex.withLock {
                if (activeTrackingPackage == packageName) {
                    sessionBaseMillis = 0L
                    sessionStartElapsed = SystemClock.elapsedRealtime()
                    Log.d(TAG, "In-memory usage reset for $packageName")
                }
            }
        }
    }

    private suspend fun handlePackageChange(newPackage: String) {
        transitionMutex.withLock {
            // If already tracking this package, do not interrupt
            if (activeTrackingPackage == newPackage) {
                return@withLock
            }

            // Pause and persist old tracking session
            pauseTrackingSessionInternal()

            // If new package is protected
            if (newPackage in protectedPackages) {
                val usage = withContext(Dispatchers.IO) { usageRepo.getTodayUsage(newPackage) }
                val label = getAppLabel(newPackage)

                if (usage.isExhausted) {
                    Log.d(TAG, "Protected app $newPackage is EXHAUSTED (${usage.usedMillis.toMinuteSecondString()} / ${usage.effectiveLimitMillis.toMinuteSecondString()}) -> blocking")
                    triggerBlockInternal(newPackage, label)
                } else {
                    startTrackingSessionInternal(newPackage, usage.usedMillis, label)
                    overlayManager?.dismissBlocking()
                }
            } else {
                // Non-protected app / launcher / settings
                activeTrackingPackage = null
                notificationManager.setIdle()
            }
        }
    }

    private fun startTrackingSessionInternal(packageName: String, initialUsedMillis: Long, label: String) {
        activeTrackingPackage = packageName
        sessionBaseMillis = initialUsedMillis
        sessionStartElapsed = SystemClock.elapsedRealtime()
        warningShown = false

        Log.d(TAG, "Tracking started: $packageName (base: ${initialUsedMillis.toMinuteSecondString()})")
        notificationManager.updateTrackingNotification(label, initialUsedMillis)

        tickerJob?.cancel()
        tickerJob = trackerScope.launch {
            while (true) {
                delay(1_000L)
                onTick(packageName, label)
            }
        }
    }

    private suspend fun onTick(packageName: String, label: String) {
        if (activeTrackingPackage != packageName) return

        val elapsedSinceStart = SystemClock.elapsedRealtime() - sessionStartElapsed
        val currentTotalUsed = sessionBaseMillis + elapsedSinceStart

        // Fetch fresh limit to account for possible mid-session unlock
        val usage = withContext(Dispatchers.IO) { usageRepo.getTodayUsage(packageName) }
        val effectiveLimit = usage.effectiveLimitMillis
        val adjustedUsed = maxOf(currentTotalUsed, usage.usedMillis)
        val remaining = maxOf(0L, effectiveLimit - adjustedUsed)

        Log.d(TAG, "Current usage for $packageName: ${adjustedUsed.toMinuteSecondString()} (remaining: ${remaining.toMinuteSecondString()})")

        // Update silent persistent notification
        notificationManager.updateTrackingNotification(label, adjustedUsed)

        // Persist periodically (every 3 seconds)
        if (elapsedSinceStart > 0 && elapsedSinceStart % 3000L < 1100L) {
            persistSessionInternal(packageName, adjustedUsed)
        }

        when {
            remaining <= 0L -> {
                Log.d(TAG, "Time expired for $packageName -> triggering block")
                persistSessionInternal(packageName, adjustedUsed)
                stopTicker()
                triggerBlockInternal(packageName, label)
            }
            remaining <= WARNING_THRESHOLD_MILLIS -> {
                val settings = settingsRepo.settings.first()
                if (!warningShown) {
                    warningShown = true
                    overlayManager?.showWarning(
                        packageLabel = label,
                        remainingMillis = remaining,
                        countdownStyle = settings.countdownStyle,
                        settings = settings
                    )
                } else {
                    overlayManager?.updateWarningCountdown(remaining)
                }
            }
            else -> {
                if (warningShown) {
                    overlayManager?.dismissWarning()
                    warningShown = false
                }
            }
        }
    }

    private suspend fun pauseTrackingSessionInternal() {
        stopTicker()
        overlayManager?.dismissWarning()
        warningShown = false

        val pkg = activeTrackingPackage ?: return
        val elapsedSinceStart = SystemClock.elapsedRealtime() - sessionStartElapsed
        if (elapsedSinceStart > 0) {
            val totalUsed = sessionBaseMillis + elapsedSinceStart
            Log.d(TAG, "Tracking stopped: $pkg (Added usage: ${elapsedSinceStart.toMinuteSecondString()}, Total: ${totalUsed.toMinuteSecondString()})")
            persistSessionInternal(pkg, totalUsed)
        }
        activeTrackingPackage = null
    }

    private suspend fun persistCurrentSession() {
        val pkg = activeTrackingPackage ?: return
        val elapsedSinceStart = SystemClock.elapsedRealtime() - sessionStartElapsed
        if (elapsedSinceStart > 0) {
            val totalUsed = sessionBaseMillis + elapsedSinceStart
            persistSessionInternal(pkg, totalUsed)
        }
    }

    private suspend fun persistSessionInternal(packageName: String, totalUsedMillis: Long) {
        val today = currentDate
        withContext(Dispatchers.IO) {
            val existing = usageRepo.getTodayUsage(packageName)
            if (totalUsedMillis > existing.usedMillis) {
                val delta = totalUsedMillis - existing.usedMillis
                usageRepo.addUsedMillis(packageName, today, delta)
                sessionBaseMillis = totalUsedMillis
                sessionStartElapsed = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    private fun triggerBlockInternal(packageName: String, label: String) {
        overlayManager?.dismissWarning()
        warningShown = false
        activeTrackingPackage = null
        notificationManager.setIdle()

        overlayManager?.showBlocking(
            packageName = packageName,
            packageLabel = label,
            unlockEnabled = unlockChallengeEnabled,
            onUnlockTap = {
                openChallengeForPackage(packageName, label)
            }
        )

        // Send user to Home screen
        sendHomeAction?.invoke()
    }

    private fun openChallengeForPackage(packageName: String, label: String) {
        overlayManager?.showChallenge(
            packageName = packageName,
            packageLabel = label,
            onSuccess = {
                trackerScope.launch {
                    Log.d(TAG, "Challenge completed successfully -> unlocking +30 mins ONLY for $packageName")
                    unlockAppUseCase(packageName)
                    overlayManager?.dismissBlocking()
                    warningShown = false

                    // If user is currently attempting to open or in this package, resume tracking
                    val usage = withContext(Dispatchers.IO) { usageRepo.getTodayUsage(packageName) }
                    transitionMutex.withLock {
                        if (currentForegroundPackage == packageName) {
                            startTrackingSessionInternal(packageName, usage.usedMillis, label)
                        }
                    }
                }
            },
            onDismiss = {
                Log.d(TAG, "Challenge dismissed for $packageName")
            }
        )
    }

    private fun observeProtectedPackages() {
        trackerScope.launch {
            protectedAppRepo.observeEnabledPackageNames().collect { pkgs ->
                protectedPackages = pkgs.toSet()
                Log.d(TAG, "Protected packages updated: $protectedPackages")
            }
        }
    }

    private fun observeSettings() {
        trackerScope.launch {
            settingsRepo.settings.collect { settings ->
                unlockChallengeEnabled = settings.unlockChallengeEnabled
            }
        }
    }

    private fun getAppLabel(packageName: String): String {
        return InstalledAppsHelper.getAppLabel(context, packageName) ?: packageName
    }
}
