package com.afnan.stopme.domain.model

/**
 * Domain model representing a protected app.
 * [displayName] is null when the app is not installed (manually added package).
 */
data class ProtectedApp(
    val packageName: String,
    val displayName: String?,
    val addedAt: Long,
    val enabled: Boolean
) {
    /** The label shown in the UI — falls back to the package name if unknown. */
    val label: String get() = displayName ?: packageName
}
