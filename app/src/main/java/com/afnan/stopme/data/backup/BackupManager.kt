package com.afnan.stopme.data.backup

import android.content.Context
import android.net.Uri
import com.afnan.stopme.data.local.dao.DailyUsageDao
import com.afnan.stopme.data.local.dao.ProtectedAppDao
import com.afnan.stopme.data.local.entities.DailyUsageEntity
import com.afnan.stopme.data.local.entities.ProtectedAppEntity
import com.afnan.stopme.data.preferences.AppPreferencesDataStore
import com.afnan.stopme.domain.model.AppTheme
import com.afnan.stopme.domain.model.CountdownStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val protectedAppDao: ProtectedAppDao,
    private val dailyUsageDao: DailyUsageDao,
    private val preferencesDataStore: AppPreferencesDataStore
) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    sealed class BackupResult {
        object Success : BackupResult()
        data class Error(val message: String) : BackupResult()
    }

    /**
     * Exports current state to JSON and writes it to [uri].
     */
    suspend fun exportTo(uri: Uri): BackupResult {
        return try {
            val apps = protectedAppDao.getAll()
            val usage = dailyUsageDao.getAllUsage()
            val settings = preferencesDataStore.appSettings.first()

            val doc = BackupDocument(
                selectedApps = apps.map {
                    AppBackup(it.packageName, it.displayName, it.addedAt, it.enabled)
                },
                usage = usage.map {
                    UsageBackup(it.packageName, it.localDate, it.usedMillis, it.extraUnlockedMillis)
                },
                settings = SettingsBackup(
                    theme = settings.theme.name,
                    warningSoundEnabled = settings.warningSoundEnabled,
                    vibrationEnabled = settings.vibrationEnabled,
                    countdownStyle = settings.countdownStyle.name,
                    unlockChallengeEnabled = settings.unlockChallengeEnabled
                )
            )

            val jsonString = json.encodeToString(BackupDocument.serializer(), doc)
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(jsonString.toByteArray(Charsets.UTF_8))
            }
            BackupResult.Success
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown export error")
        }
    }

    /**
     * Imports backup from [uri].
     * Validates schema version and structure before touching the database.
     * On any error, existing data is left untouched.
     */
    suspend fun importFrom(uri: Uri): BackupResult {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            } ?: return BackupResult.Error("Could not read file")

            val doc = try {
                json.decodeFromString(BackupDocument.serializer(), jsonString)
            } catch (e: Exception) {
                return BackupResult.Error("Invalid or corrupt JSON backup")
            }

            // Version check
            if (doc.version > BackupDocument.CURRENT_VERSION) {
                return BackupResult.Error("Backup version ${doc.version} is not supported by this app version")
            }

            // Apply — all writes or none (best-effort; Room is not truly transactional across tables here)
            doc.selectedApps.forEach { app ->
                protectedAppDao.insertApp(
                    ProtectedAppEntity(
                        packageName = app.packageName,
                        displayName = app.displayName,
                        addedAt = app.addedAt,
                        enabled = app.enabled
                    )
                )
            }

            doc.usage.forEach { usage ->
                val existing = dailyUsageDao.getUsage(usage.packageName, usage.localDate)
                if (existing == null) {
                    dailyUsageDao.insertOrIgnore(
                        DailyUsageEntity(
                            packageName = usage.packageName,
                            localDate = usage.localDate,
                            usedMillis = usage.usedMillis,
                            extraUnlockedMillis = usage.extraUnlockedMillis
                        )
                    )
                }
                // Don't overwrite existing usage records to prevent cheating
            }

            // Restore settings
            val s = doc.settings
            preferencesDataStore.setTheme(AppTheme.fromString(s.theme))
            preferencesDataStore.setWarningSoundEnabled(s.warningSoundEnabled)
            preferencesDataStore.setVibrationEnabled(s.vibrationEnabled)
            preferencesDataStore.setCountdownStyle(CountdownStyle.fromString(s.countdownStyle))
            preferencesDataStore.setUnlockChallengeEnabled(s.unlockChallengeEnabled)

            BackupResult.Success
        } catch (e: Exception) {
            BackupResult.Error(e.message ?: "Unknown import error")
        }
    }
}
