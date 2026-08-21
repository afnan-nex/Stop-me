package com.afnan.stopme.core.common.utils

import java.time.LocalDate

/**
 * Returns the current local date using the device's default timezone.
 * Used everywhere date comparisons are needed to ensure consistency.
 */
fun todayLocalDate(): LocalDate = LocalDate.now()

/**
 * Formats a [LocalDate] to the ISO-8601 string form (e.g. "2025-08-20").
 */
fun LocalDate.toDateString(): String = this.toString()

/**
 * Parses an ISO-8601 date string back to [LocalDate].
 * Returns null if the string is invalid.
 */
fun String.toLocalDateOrNull(): LocalDate? = try {
    LocalDate.parse(this)
} catch (e: Exception) {
    null
}
