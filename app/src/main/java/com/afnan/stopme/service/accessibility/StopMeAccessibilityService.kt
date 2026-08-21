package com.afnan.stopme.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.afnan.stopme.service.notification.UsageNotificationManager
import com.afnan.stopme.service.overlay.OverlayManager
import com.afnan.stopme.service.tracker.ForegroundAppTracker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Android Accessibility Service that listens for window transitions
 * and delegates foreground application tracking to [ForegroundAppTracker].
 */
@AndroidEntryPoint
class StopMeAccessibilityService : AccessibilityService() {

    @Inject lateinit var foregroundAppTracker: ForegroundAppTracker
    @Inject lateinit var notificationManager: UsageNotificationManager

    private lateinit var overlayManager: OverlayManager

    override fun onServiceConnected() {
        super.onServiceConnected()
        overlayManager = OverlayManager(this)

        // Start foreground service with silent idle notification
        startForeground(
            UsageNotificationManager.NOTIFICATION_ID,
            notificationManager.createIdleNotification()
        )

        foregroundAppTracker.attach(
            overlayManager = overlayManager,
            sendHome = {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
        )
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventPkg = event.packageName?.toString()
        val activeWindowPkg = try {
            rootInActiveWindow?.packageName?.toString()
        } catch (e: Exception) {
            null
        }

        // Determine the actual foreground package, ignoring transient system overlays & keyboards
        val detectedPkg = when {
            activeWindowPkg != null && !isIgnoredPackage(activeWindowPkg) -> activeWindowPkg
            eventPkg != null && !isIgnoredPackage(eventPkg) -> eventPkg
            else -> null
        }

        if (detectedPkg != null) {
            foregroundAppTracker.onPackageDetected(detectedPkg)
        }
    }

    override fun onInterrupt() {
        // Service interrupted
    }

    override fun onDestroy() {
        foregroundAppTracker.detach()
        overlayManager.destroyAll()
        super.onDestroy()
    }

    private fun isIgnoredPackage(pkg: String): Boolean {
        // 1. Android OS & System UI framework overlays (notifications, nav bar, volume, status bar)
        if (pkg == "android" || pkg == "com.android.systemui") return true

        // 3. Known keyboard / Input Method packages
        val lower = pkg.lowercase()
        if (lower.contains("inputmethod") ||
            lower.contains("keyboard") ||
            lower.contains("honeyboard") ||
            pkg == "com.google.android.inputmethod.latin" ||
            pkg == "com.touchtype.swiftkey" ||
            pkg == "com.samsung.android.honeyboard" ||
            pkg == "com.syntellia.fleksy.keyboard"
        ) return true

        // 4. Transient system permission & intelligence dialogs
        if (pkg.startsWith("com.android.permissioncontroller") ||
            pkg.startsWith("com.google.android.permissioncontroller") ||
            pkg == "com.android.settings.intelligence"
        ) return true

        return false
    }
}
