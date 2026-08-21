package com.afnan.stopme.feature.apps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.stopme.domain.model.AppWithUsage
import com.afnan.stopme.domain.repository.UsageRepository
import com.afnan.stopme.domain.usecase.AddAppResult
import com.afnan.stopme.domain.usecase.AddProtectedAppUseCase
import com.afnan.stopme.domain.usecase.GetProtectedAppsWithUsageUseCase
import com.afnan.stopme.service.tracker.ForegroundAppTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val getAppsWithUsage: GetProtectedAppsWithUsageUseCase,
    private val addApp: AddProtectedAppUseCase,
    private val usageRepo: UsageRepository,
    private val foregroundAppTracker: ForegroundAppTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAppsWithUsage().collect { apps ->
                _uiState.update { state ->
                    state.copy(
                        apps = apps,
                        isLoading = false,
                        totalUsedMillisToday = apps.sumOf { it.usage.usedMillis }
                    )
                }
            }
        }
    }

    fun showAppSelector() {
        _uiState.update { it.copy(showAppSelector = true) }
    }

    fun hideAppSelector() {
        _uiState.update { it.copy(showAppSelector = false) }
    }

    fun addPackage(packageName: String) {
        viewModelScope.launch {
            when (val result = addApp(packageName)) {
                is AddAppResult.Success -> {
                    _uiState.update { it.copy(showAppSelector = false, errorMessage = null) }
                }
                is AddAppResult.Duplicate -> {
                    _uiState.update { it.copy(errorMessage = "This package is already protected.") }
                }
                is AddAppResult.InvalidPackage -> {
                    _uiState.update { it.copy(errorMessage = "Invalid package name.") }
                }
            }
        }
    }

    /**
     * Triggered on long-press of a protected app row.
     * Shows the "Reset Countdown" confirmation dialog.
     */
    fun onAppLongClick(appWithUsage: AppWithUsage) {
        _uiState.update { it.copy(selectedAppForReset = appWithUsage) }
    }

    fun dismissResetDialog() {
        _uiState.update { it.copy(selectedAppForReset = null) }
    }

    /**
     * Resets the selected app's countdown for today back to 0 used time (full allowance).
     */
    fun confirmResetCountdown() {
        val app = _uiState.value.selectedAppForReset ?: return
        viewModelScope.launch {
            usageRepo.resetTodayUsage(app.packageName)
            foregroundAppTracker.onResetUsage(app.packageName)
            _uiState.update { it.copy(selectedAppForReset = null) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
