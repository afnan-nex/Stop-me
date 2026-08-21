package com.afnan.stopme.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.afnan.stopme.domain.model.AppSettings
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.domain.model.CountdownStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val appSettings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            theme = AppTheme.fromString(prefs[PreferencesKeys.THEME]),
            warningSoundEnabled = prefs[PreferencesKeys.WARNING_SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[PreferencesKeys.VIBRATION_ENABLED] ?: true,
            countdownStyle = CountdownStyle.fromString(prefs[PreferencesKeys.COUNTDOWN_STYLE]),
            unlockChallengeEnabled = prefs[PreferencesKeys.UNLOCK_CHALLENGE_ENABLED] ?: true,
            onboardingComplete = prefs[PreferencesKeys.ONBOARDING_COMPLETE] ?: false
        )
    }

    suspend fun setTheme(theme: AppTheme) {
        dataStore.edit { it[PreferencesKeys.THEME] = theme.name }
    }

    suspend fun setWarningSoundEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.WARNING_SOUND_ENABLED] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.VIBRATION_ENABLED] = enabled }
    }

    suspend fun setCountdownStyle(style: CountdownStyle) {
        dataStore.edit { it[PreferencesKeys.COUNTDOWN_STYLE] = style.name }
    }

    suspend fun setUnlockChallengeEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.UNLOCK_CHALLENGE_ENABLED] = enabled }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETE] = complete }
    }
}
