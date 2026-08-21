package com.afnan.stopme.domain.model

enum class AppTheme {
    SYSTEM, LIGHT, DARK;

    companion object {
        fun fromString(value: String?): AppTheme = when (value) {
            "LIGHT" -> LIGHT
            "DARK" -> DARK
            else -> SYSTEM
        }
    }
}
