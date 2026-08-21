package com.afnan.stopme.service.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.KeyEvent
import android.view.WindowManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.afnan.stopme.core.designsystem.theme.StopMeTheme
import com.afnan.stopme.core.platform.ServiceLifecycleOwner
import com.afnan.stopme.domain.model.AppSettings
import com.afnan.stopme.domain.model.CountdownStyle
import com.afnan.stopme.feature.overlay.BlockingOverlayContent
import com.afnan.stopme.feature.overlay.UnlockChallengeOverlayContent
import com.afnan.stopme.feature.overlay.WarningOverlayContent

/**
 * Manages all WindowManager overlay windows for the accessibility service.
 *
 * Three overlay types:
 *  - Warning: small top banner counting down the last 30 seconds
 *  - Blocking: full-screen block when time is exhausted (with Close app button and Back key intercept)
 *  - Challenge: full-screen unlock challenge flow
 */
class OverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val lifecycleOwner = ServiceLifecycleOwner()

    // State holders that drive Compose recomposition without recreating views
    private val warningState: MutableState<WarningOverlayState?> = mutableStateOf(null)
    private val blockingState: MutableState<BlockingOverlayState?> = mutableStateOf(null)
    private val challengeState: MutableState<ChallengeOverlayState?> = mutableStateOf(null)

    // Actual window views
    private var warningView: ComposeView? = null
    private var blockingView: ComposeView? = null
    private var challengeView: ComposeView? = null

    init {
        lifecycleOwner.onCreate()
        lifecycleOwner.onResume()
    }

    // ─── Warning Overlay ──────────────────────────────────────────────────────

    fun showWarning(
        packageLabel: String,
        remainingMillis: Long,
        countdownStyle: CountdownStyle,
        settings: AppSettings
    ) {
        warningState.value = WarningOverlayState(packageLabel, remainingMillis, countdownStyle)
        if (warningView == null) {
            warningView = createComposeView {
                val state = warningState.value ?: return@createComposeView
                StopMeTheme {
                    WarningOverlayContent(state = state)
                }
            }
            try {
                windowManager.addView(warningView, warningParams())
            } catch (e: Exception) {
                warningView = null
            }
        }
    }

    fun updateWarningCountdown(remainingMillis: Long) {
        warningState.value = warningState.value?.copy(remainingMillis = remainingMillis)
    }

    fun dismissWarning() {
        removeView(warningView)
        warningView = null
        warningState.value = null
    }

    // ─── Blocking Overlay ─────────────────────────────────────────────────────

    fun showBlocking(
        packageName: String,
        packageLabel: String,
        onCloseApp: () -> Unit
    ) {
        blockingState.value = BlockingOverlayState(
            packageName = packageName,
            packageLabel = packageLabel,
            onCloseApp = onCloseApp
        )
        if (blockingView == null) {
            blockingView = createComposeView(
                onBackPressed = {
                    blockingState.value?.onCloseApp?.invoke()
                }
            ) {
                val state = blockingState.value ?: return@createComposeView
                StopMeTheme {
                    BlockingOverlayContent(
                        packageLabel = state.packageLabel,
                        onCloseApp = state.onCloseApp
                    )
                }
            }
            try {
                windowManager.addView(blockingView, blockingParams())
            } catch (e: Exception) {
                blockingView = null
            }
        } else {
            // Update state for existing view
            blockingState.value = blockingState.value?.copy(
                packageName = packageName,
                packageLabel = packageLabel,
                onCloseApp = onCloseApp
            )
        }
    }

    fun dismissBlocking() {
        removeView(blockingView)
        blockingView = null
        blockingState.value = null
        // Also dismiss challenge if open
        dismissChallenge()
    }

    val isBlockingVisible: Boolean get() = blockingView != null

    // ─── Challenge Overlay ────────────────────────────────────────────────────

    fun showChallenge(
        packageName: String,
        packageLabel: String,
        onSuccess: () -> Unit,
        onDismiss: () -> Unit
    ) {
        if (challengeView != null) return // Already showing
        challengeState.value = ChallengeOverlayState(
            packageName = packageName,
            packageLabel = packageLabel,
            onSuccess = onSuccess,
            onDismiss = onDismiss
        )
        challengeView = createComposeView(
            onBackPressed = onDismiss
        ) {
            val state = challengeState.value ?: return@createComposeView
            StopMeTheme {
                UnlockChallengeOverlayContent(
                    packageLabel = state.packageLabel,
                    onSuccess = state.onSuccess,
                    onDismiss = {
                        dismissChallenge()
                        state.onDismiss()
                    }
                )
            }
        }
        try {
            windowManager.addView(challengeView, challengeParams())
        } catch (e: Exception) {
            challengeView = null
        }
    }

    fun dismissChallenge() {
        removeView(challengeView)
        challengeView = null
        challengeState.value = null
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    fun destroyAll() {
        dismissWarning()
        dismissBlocking()
        dismissChallenge()
        lifecycleOwner.onDestroy()
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private fun createComposeView(
        onBackPressed: (() -> Unit)? = null,
        content: @androidx.compose.runtime.Composable () -> Unit
    ): ComposeView {
        return ComposeView(context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    onBackPressed?.invoke()
                    true
                } else {
                    false
                }
            }
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent { content() }
        }
    }

    private fun removeView(view: ComposeView?) {
        view?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                // Already removed
            }
        }
    }

    private fun baseParams(
        width: Int = WindowManager.LayoutParams.MATCH_PARENT,
        height: Int = WindowManager.LayoutParams.MATCH_PARENT,
        flags: Int = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    ) = WindowManager.LayoutParams(
        width, height,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        flags,
        PixelFormat.TRANSLUCENT
    )

    private fun warningParams() = baseParams(
        height = WindowManager.LayoutParams.WRAP_CONTENT,
        flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    ).apply {
        gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
    }

    private fun blockingParams() = baseParams(
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    )

    private fun challengeParams() = baseParams(
        flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    )
}

// ─── State data classes ───────────────────────────────────────────────────────

data class WarningOverlayState(
    val packageLabel: String,
    val remainingMillis: Long,
    val countdownStyle: CountdownStyle
)

data class BlockingOverlayState(
    val packageName: String,
    val packageLabel: String,
    val onCloseApp: () -> Unit
)

data class ChallengeOverlayState(
    val packageName: String,
    val packageLabel: String,
    val onSuccess: () -> Unit,
    val onDismiss: () -> Unit
)
