package com.afnan.stopme.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.domain.model.AppWithUsage
import com.afnan.stopme.domain.model.CountdownStyle
import com.afnan.stopme.domain.repository.SettingsRepository
import com.afnan.stopme.domain.usecase.GetProtectedAppsWithUsageUseCase
import com.afnan.stopme.domain.usecase.RemoveProtectedAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepository,
    private val getProtectedApps: GetProtectedAppsWithUsageUseCase,
    private val removeAppUseCase: RemoveProtectedAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepo.settings.collect { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getProtectedApps().collect { apps ->
                _uiState.update { it.copy(protectedApps = apps) }
            }
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch { settingsRepo.setTheme(theme) }
    }

    fun setWarningSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setWarningSoundEnabled(enabled) }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setVibrationEnabled(enabled) }
    }

    fun setCountdownStyle(style: CountdownStyle) {
        viewModelScope.launch { settingsRepo.setCountdownStyle(style) }
    }

    fun setUnlockChallengeEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setUnlockChallengeEnabled(enabled) }
    }

    fun showProtectedAppsDialog() {
        _uiState.update { it.copy(showProtectedAppsDialog = true) }
    }

    fun hideProtectedAppsDialog() {
        _uiState.update { it.copy(showProtectedAppsDialog = false) }
    }

    fun requestRemoveApp(app: AppWithUsage) {
        _uiState.update { it.copy(pendingRemoveApp = app) }
    }

    fun cancelRemoveApp() {
        _uiState.update { it.copy(pendingRemoveApp = null) }
    }

    fun confirmRemoveApp() {
        val app = _uiState.value.pendingRemoveApp ?: return
        viewModelScope.launch {
            removeAppUseCase(app.packageName)
            _uiState.update { it.copy(pendingRemoveApp = null) }
        }
    }
}
