package com.afnan.stopme.feature.settings

import com.afnan.stopme.data.backup.BackupManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint to access BackupManager from a non-injected composable context.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackupManagerEntryPoint {
    fun backupManager(): BackupManager
}
