package com.afnan.stopme.domain.model

/**
 * Combines a [ProtectedApp] with its [DailyUsage] for the current day.
 * If no usage record exists for today, [usage] defaults to a fresh record.
 */
data class AppWithUsage(
    val app: ProtectedApp,
    val usage: DailyUsage
) {
    val packageName: String get() = app.packageName
    val label: String get() = app.label
}
