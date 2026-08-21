package com.afnan.stopme.data.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val THEME = stringPreferencesKey("theme")
    val WARNING_SOUND_ENABLED = booleanPreferencesKey("warning_sound_enabled")
    val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    val COUNTDOWN_STYLE = stringPreferencesKey("countdown_style")
    val UNLOCK_CHALLENGE_ENABLED = booleanPreferencesKey("unlock_challenge_enabled")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
}
