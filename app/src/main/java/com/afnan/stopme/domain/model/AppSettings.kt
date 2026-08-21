package com.afnan.stopme.domain.model

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val warningSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val countdownStyle: CountdownStyle = CountdownStyle.PILL,
    val unlockChallengeEnabled: Boolean = true,
    val onboardingComplete: Boolean = false
)
