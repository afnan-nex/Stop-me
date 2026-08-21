package com.afnan.stopme.domain.repository

import com.afnan.stopme.domain.model.AppSettings
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.domain.model.CountdownStyle
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setWarningSoundEnabled(enabled: Boolean)
    suspend fun setVibrationEnabled(enabled: Boolean)
    suspend fun setCountdownStyle(style: CountdownStyle)
    suspend fun setUnlockChallengeEnabled(enabled: Boolean)
    suspend fun setOnboardingComplete(complete: Boolean)
}
