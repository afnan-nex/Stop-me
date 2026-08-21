package com.afnan.stopme.domain.usecase

import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import com.afnan.stopme.domain.model.AppWithUsage
import com.afnan.stopme.domain.model.DailyUsage
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import com.afnan.stopme.domain.repository.UsageRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Returns the last 7 days of usage for all protected apps, grouped by date.
 */
class GetWeeklyUsageUseCase @Inject constructor(
    private val appRepo: ProtectedAppRepository,
    private val usageRepo: UsageRepository
) {
    suspend operator fun invoke(): Map<String, List<DailyUsage>> {
        val today = todayLocalDate()
        val startDate = today.minusDays(6).toDateString()
        val endDate = today.toDateString()

        val allUsage = usageRepo.getAllUsageForRange(startDate, endDate)
        return allUsage.groupBy { it.localDate }
    }
}
