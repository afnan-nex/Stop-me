package com.afnan.stopme.feature.onboarding

data class PermissionState(
    val overlayGranted: Boolean = false,
    val usageAccessGranted: Boolean = false,
    val accessibilityEnabled: Boolean = false,
    val batteryOptimizationIgnored: Boolean = false,
    val notificationsEnabled: Boolean = false
) {
    val allGranted: Boolean
        get() = overlayGranted && usageAccessGranted && accessibilityEnabled && notificationsEnabled
    // Battery optimization is strongly recommended but not required to proceed
}
