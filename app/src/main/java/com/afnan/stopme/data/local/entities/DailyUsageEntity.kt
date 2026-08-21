package com.afnan.stopme.data.local.entities

import androidx.room.Entity

/**
 * Tracks per-app usage for a specific local calendar day.
 *
 * Composite primary key: [packageName] + [localDate] (ISO-8601, e.g. "2025-08-20").
 *
 * Effective daily allowance = [DAILY_LIMIT_MILLIS] + [extraUnlockedMillis].
 * Remaining = max(0, effective allowance - [usedMillis]).
 */
@Entity(
    tableName = "daily_usage",
    primaryKeys = ["packageName", "localDate"]
)
data class DailyUsageEntity(
    val packageName: String,
    val localDate: String,               // ISO-8601 local date string
    val usedMillis: Long = 0L,           // milliseconds of actual foreground usage today
    val extraUnlockedMillis: Long = 0L   // additional time granted via unlock challenge
)
