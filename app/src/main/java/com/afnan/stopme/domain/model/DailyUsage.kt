package com.afnan.stopme.domain.model

import com.afnan.stopme.core.common.utils.DAILY_LIMIT_MILLIS

/**
 * Domain model for a single app's usage on a single local calendar day.
 */
data class DailyUsage(
    val packageName: String,
    val localDate: String,
    val usedMillis: Long,
    val extraUnlockedMillis: Long
) {
    /** Total allowed time for the day */
    val effectiveLimitMillis: Long get() = DAILY_LIMIT_MILLIS + extraUnlockedMillis

    /** Remaining time; clamped to 0 */
    val remainingMillis: Long get() = maxOf(0L, effectiveLimitMillis - usedMillis)

    /** True when no time remains */
    val isExhausted: Boolean get() = remainingMillis <= 0L

    /** Fraction used (0.0 – 1.0) for progress bars */
    val fractionUsed: Float
        get() = if (effectiveLimitMillis <= 0L) 1f
                else (usedMillis.toFloat() / effectiveLimitMillis.toFloat()).coerceIn(0f, 1f)
}
