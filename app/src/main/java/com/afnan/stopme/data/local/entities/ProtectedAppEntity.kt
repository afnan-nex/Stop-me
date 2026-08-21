package com.afnan.stopme.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents an app the user has chosen to protect with Stop-me.
 * [packageName] is the canonical identifier — must be unique.
 * [displayName] is nullable because a manually-added package may not be installed.
 */
@Entity(tableName = "protected_apps")
data class ProtectedAppEntity(
    @PrimaryKey
    val packageName: String,
    val displayName: String?,
    val addedAt: Long = System.currentTimeMillis(),
    val enabled: Boolean = true
)
