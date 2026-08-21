package com.afnan.stopme.domain.usecase

import com.afnan.stopme.domain.model.AppWithUsage
import com.afnan.stopme.domain.model.DailyUsage
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import com.afnan.stopme.domain.repository.UsageRepository
import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetProtectedAppsWithUsageUseCase @Inject constructor(
    private val appRepo: ProtectedAppRepository,
    private val usageRepo: UsageRepository
) {
    /**
     * Returns a reactive [Flow] emitting the full [AppWithUsage] list whenever
     * either the protected-app list or today's usage changes.
     */
    operator fun invoke(): Flow<List<AppWithUsage>> {
        val today = todayLocalDate().toDateString()
        return combine(
            appRepo.observeAll(),
            usageRepo.observeAllTodayUsage()
        ) { apps, usageList ->
            val usageMap = usageList.associateBy { it.packageName }
            apps.map { app ->
                AppWithUsage(
                    app = app,
                    usage = usageMap[app.packageName]
                        ?: DailyUsage(app.packageName, today, 0L, 0L)
                )
            }
        }
    }
}
