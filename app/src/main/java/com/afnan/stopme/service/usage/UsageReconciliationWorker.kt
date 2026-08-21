package com.afnan.stopme.service.usage

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import com.afnan.stopme.domain.repository.UsageRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@HiltWorker
class UsageReconciliationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val protectedAppRepo: ProtectedAppRepository,
    private val usageRepo: UsageRepository,
    private val usageStatsHelper: UsageStatsHelper
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val today = todayLocalDate().toDateString()
            val enabledPackages = protectedAppRepo.getEnabledPackageNames()

            if (enabledPackages.isEmpty()) return Result.success()

            // Query UsageStats for today
            val statsMap = usageStatsHelper.queryTodayUsage(enabledPackages.toSet())

            // Reconcile: if UsageStats shows more usage than our DB, update to be safe
            for (pkg in enabledPackages) {
                val statsMillis = statsMap[pkg] ?: 0L
                val dbUsage = usageRepo.getTodayUsage(pkg)

                if (statsMillis > dbUsage.usedMillis) {
                    // UsageStats found more — update (could happen after service interruption)
                    usageRepo.ensureRecord(pkg, today)
                    val delta = statsMillis - dbUsage.usedMillis
                    usageRepo.addUsedMillis(pkg, today, delta)
                }
            }

            // Clean up old usage records (keep last 30 days)
            val thirtyDaysAgo = LocalDate.now().minusDays(30).toDateString()
            usageRepo.deleteOlderThan(thirtyDaysAgo)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "usage_reconciliation"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UsageReconciliationWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
