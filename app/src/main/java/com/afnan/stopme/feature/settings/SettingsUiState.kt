package com.afnan.stopme.feature.settings

import com.afnan.stopme.domain.model.AppSettings
import com.afnan.stopme.domain.model.AppWithUsage

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val protectedApps: List<AppWithUsage> = emptyList(),
    val showProtectedAppsDialog: Boolean = false,
    val pendingRemoveApp: AppWithUsage? = null,
    val isLoading: Boolean = true
)
