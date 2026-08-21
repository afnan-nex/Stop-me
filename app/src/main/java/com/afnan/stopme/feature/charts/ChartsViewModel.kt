package com.afnan.stopme.feature.charts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.afnan.stopme.domain.usecase.GetWeeklyUsageUseCase
import com.afnan.stopme.domain.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val usageRepo: UsageRepository,
    private val getWeeklyUsage: GetWeeklyUsageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChartsUiState())
    val uiState: StateFlow<ChartsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Observe today's usage reactively
            usageRepo.observeAllTodayUsage().collect { todayUsage ->
                _uiState.update { it.copy(todayUsage = todayUsage, isLoading = false) }
                // Also refresh weekly on each update
                val weekly = getWeeklyUsage()
                _uiState.update { it.copy(weeklyUsage = weekly) }
            }
        }
    }
}
