package com.afnan.stopme.feature.apps

import com.afnan.stopme.domain.model.AppWithUsage

data class AppsUiState(
    val apps: List<AppWithUsage> = emptyList(),
    val isLoading: Boolean = true,
    val totalUsedMillisToday: Long = 0L,
    val showAppSelector: Boolean = false,
    val selectedAppForReset: AppWithUsage? = null, // App selected via long-press for countdown reset confirmation
    val errorMessage: String? = null
)
