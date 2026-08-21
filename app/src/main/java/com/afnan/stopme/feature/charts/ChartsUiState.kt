package com.afnan.stopme.feature.charts

import com.afnan.stopme.domain.model.DailyUsage

data class ChartsUiState(
    val todayUsage: List<DailyUsage> = emptyList(),
    val weeklyUsage: Map<String, List<DailyUsage>> = emptyMap(),
    val isLoading: Boolean = true
)
