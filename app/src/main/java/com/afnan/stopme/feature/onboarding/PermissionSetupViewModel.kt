package com.afnan.stopme.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.stopme.core.common.extensions.areNotificationsEnabled
import com.afnan.stopme.core.common.extensions.hasOverlayPermission
import com.afnan.stopme.core.common.extensions.hasUsageAccessPermission
import com.afnan.stopme.core.common.extensions.isAccessibilityServiceEnabled
import com.afnan.stopme.core.common.extensions.isBatteryOptimizationIgnored
import com.afnan.stopme.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionSetupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepo: SettingsRepository
) : ViewModel() {

    private val _permissionState = MutableStateFlow(PermissionState())
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    fun refresh() {
        _permissionState.value = PermissionState(
            overlayGranted = context.hasOverlayPermission(),
            usageAccessGranted = context.hasUsageAccessPermission(),
            accessibilityEnabled = context.isAccessibilityServiceEnabled(),
            batteryOptimizationIgnored = context.isBatteryOptimizationIgnored(),
            notificationsEnabled = context.areNotificationsEnabled()
        )
    }

    fun markOnboardingComplete() {
        viewModelScope.launch {
            settingsRepo.setOnboardingComplete(true)
        }
    }
}
