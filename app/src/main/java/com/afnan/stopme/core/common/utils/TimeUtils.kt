package com.afnan.stopme.core.common.utils

import java.util.concurrent.TimeUnit

/**
 * Formats milliseconds to a human-readable minute string like "18m" or "1h 18m".
 * Used in the app list and charts screens.
 */
fun Long.toMinuteString(): String {
    if (this <= 0L) return "0m"
    val totalMinutes = TimeUnit.MILLISECONDS.toMinutes(this)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}

/**
 * Formats milliseconds to countdown format "MM:SS".
 * Used in the warning overlay ticker.
 */
fun Long.toCountdownString(): String {
    val totalSeconds = maxOf(0L, TimeUnit.MILLISECONDS.toSeconds(this))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Formats milliseconds to a short "Xm left" or "0m left" string.
 */
fun Long.toRemainingString(): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(maxOf(0L, this))
    return "${minutes}m left"
}

/**
 * Formats milliseconds as "MM:SS" (e.g. "10:24", "25:32", "03:17").
 * Used in active usage tracking notifications and diagnostics.
 */
fun Long.toMinuteSecondString(): String {
    val totalSeconds = maxOf(0L, TimeUnit.MILLISECONDS.toSeconds(this))
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

/**
 * Constants for the daily limit.
 */
const val DAILY_LIMIT_MILLIS = 30L * 60L * 1000L   // 30 minutes
const val WARNING_THRESHOLD_MILLIS = 30L * 1000L    // 30 seconds

