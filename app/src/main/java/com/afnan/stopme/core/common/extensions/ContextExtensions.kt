package com.afnan.stopme.core.common.extensions

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.core.app.NotificationManagerCompat
import com.afnan.stopme.service.accessibility.StopMeAccessibilityService

/**
 * Returns true if the SYSTEM_ALERT_WINDOW (overlay) permission is granted.
 */
fun Context.hasOverlayPermission(): Boolean = Settings.canDrawOverlays(this)

/**
 * Returns true if the app has been granted usage-access via AppOpsManager.
 */
fun Context.hasUsageAccessPermission(): Boolean {
    return try {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                packageName
            )
        }
        mode == AppOpsManager.MODE_ALLOWED
    } catch (e: Exception) {
        false
    }
}

/**
 * Returns true if StopMeAccessibilityService is currently enabled in system settings.
 */
fun Context.isAccessibilityServiceEnabled(): Boolean {
    val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_GENERIC
    )
    return enabledServices.any { info ->
        info.resolveInfo.serviceInfo.packageName == packageName &&
            info.resolveInfo.serviceInfo.name == StopMeAccessibilityService::class.java.name
    }
}

/**
 * Returns true if the app is ignoring battery optimizations (i.e., Doze is bypassed).
 */
fun Context.isBatteryOptimizationIgnored(): Boolean {
    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
    return pm.isIgnoringBatteryOptimizations(packageName)
}

/**
 * Returns true if notifications are enabled for this app.
 */
fun Context.areNotificationsEnabled(): Boolean {
    return NotificationManagerCompat.from(this).areNotificationsEnabled()
}
