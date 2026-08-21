package com.afnan.stopme.data.repository

import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import com.afnan.stopme.data.local.dao.DailyUsageDao
import com.afnan.stopme.data.local.entities.DailyUsageEntity
import com.afnan.stopme.domain.model.DailyUsage
import com.afnan.stopme.domain.repository.UsageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageRepositoryImpl @Inject constructor(
    private val dao: DailyUsageDao
) : UsageRepository {

    override fun observeTodayUsage(packageName: String): Flow<DailyUsage> {
        val today = todayLocalDate().toDateString()
        return dao.observeUsage(packageName, today).map { entity ->
            entity?.toDomain() ?: DailyUsage(packageName, today, 0L, 0L)
        }
    }

    override fun observeAllTodayUsage(): Flow<List<DailyUsage>> {
        val today = todayLocalDate().toDateString()
        return dao.observeAllUsageForDate(today).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun ensureRecord(packageName: String, localDate: String) {
        dao.insertOrIgnore(DailyUsageEntity(packageName, localDate, 0L, 0L))
    }

    override suspend fun addUsedMillis(packageName: String, localDate: String, deltaMillis: Long) {
        // Ensure a record exists before incrementing
        ensureRecord(packageName, localDate)
        dao.addUsedMillis(packageName, localDate, deltaMillis)
    }

    override suspend fun addExtraUnlockedMillis(
        packageName: String,
        localDate: String,
        extraMillis: Long
    ) {
        ensureRecord(packageName, localDate)
        dao.addExtraUnlockedMillis(packageName, localDate, extraMillis)
    }

    override suspend fun resetTodayUsage(packageName: String) {
        val today = todayLocalDate().toDateString()
        ensureRecord(packageName, today)
        dao.resetUsage(packageName, today)
    }

    override suspend fun getTodayUsage(packageName: String): DailyUsage {
        val today = todayLocalDate().toDateString()
        return dao.getUsage(packageName, today)?.toDomain()
            ?: DailyUsage(packageName, today, 0L, 0L)
    }

    override suspend fun getAllTodayUsage(): List<DailyUsage> {
        val today = todayLocalDate().toDateString()
        return dao.getAllUsageForDate(today).map { it.toDomain() }
    }

    override suspend fun getUsageForRange(
        packageName: String,
        startDate: String,
        endDate: String
    ): List<DailyUsage> =
        dao.getUsageForRange(packageName, startDate, endDate).map { it.toDomain() }

    override suspend fun getAllUsageForRange(startDate: String, endDate: String): List<DailyUsage> =
        dao.getAllUsageForRange(startDate, endDate).map { it.toDomain() }

    override suspend fun deleteOlderThan(beforeDate: String) {
        dao.deleteOlderThan(beforeDate)
    }

    private fun DailyUsageEntity.toDomain() = DailyUsage(
        packageName = packageName,
        localDate = localDate,
        usedMillis = usedMillis,
        extraUnlockedMillis = extraUnlockedMillis
    )
}
