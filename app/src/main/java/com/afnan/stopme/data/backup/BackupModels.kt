package com.afnan.stopme.data.backup

import kotlinx.serialization.Serializable

/**
 * Root backup document. [version] allows future schema migrations.
 */
@Serializable
data class BackupDocument(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val selectedApps: List<AppBackup> = emptyList(),
    val usage: List<UsageBackup> = emptyList(),
    val settings: SettingsBackup = SettingsBackup()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@Serializable
data class AppBackup(
    val packageName: String,
    val displayName: String? = null,
    val addedAt: Long = 0L,
    val enabled: Boolean = true
)

@Serializable
data class UsageBackup(
    val packageName: String,
    val localDate: String,
    val usedMillis: Long = 0L,
    val extraUnlockedMillis: Long = 0L
)

@Serializable
data class SettingsBackup(
    val theme: String = "SYSTEM",
    val warningSoundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val countdownStyle: String = "PILL",
    val unlockChallengeEnabled: Boolean = true
)
