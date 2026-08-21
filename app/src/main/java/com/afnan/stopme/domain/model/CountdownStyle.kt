package com.afnan.stopme.domain.model

enum class CountdownStyle {
    PILL, MINIMAL, BOLD;

    companion object {
        fun fromString(value: String?): CountdownStyle = when (value) {
            "MINIMAL" -> MINIMAL
            "BOLD" -> BOLD
            else -> PILL
        }
    }
}
