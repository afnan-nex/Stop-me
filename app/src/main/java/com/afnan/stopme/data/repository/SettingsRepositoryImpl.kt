package com.afnan.stopme.data.repository

import com.afnan.stopme.data.preferences.AppPreferencesDataStore
import com.afnan.stopme.domain.model.AppSettings
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.domain.model.CountdownStyle
import com.afnan.stopme.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: AppPreferencesDataStore
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.appSettings

    override suspend fun setTheme(theme: AppTheme) = dataStore.setTheme(theme)
    override suspend fun setWarningSoundEnabled(enabled: Boolean) = dataStore.setWarningSoundEnabled(enabled)
    override suspend fun setVibrationEnabled(enabled: Boolean) = dataStore.setVibrationEnabled(enabled)
    override suspend fun setCountdownStyle(style: CountdownStyle) = dataStore.setCountdownStyle(style)
    override suspend fun setUnlockChallengeEnabled(enabled: Boolean) = dataStore.setUnlockChallengeEnabled(enabled)
    override suspend fun setOnboardingComplete(complete: Boolean) = dataStore.setOnboardingComplete(complete)
}
