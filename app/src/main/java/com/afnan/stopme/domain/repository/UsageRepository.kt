package com.afnan.stopme.domain.repository

import com.afnan.stopme.domain.model.DailyUsage
import kotlinx.coroutines.flow.Flow

interface UsageRepository {
    fun observeTodayUsage(packageName: String): Flow<DailyUsage>
    fun observeAllTodayUsage(): Flow<List<DailyUsage>>
    suspend fun ensureRecord(packageName: String, localDate: String)
    suspend fun addUsedMillis(packageName: String, localDate: String, deltaMillis: Long)
    suspend fun addExtraUnlockedMillis(packageName: String, localDate: String, extraMillis: Long)
    suspend fun resetTodayUsage(packageName: String)
    suspend fun getTodayUsage(packageName: String): DailyUsage
    suspend fun getAllTodayUsage(): List<DailyUsage>
    suspend fun getUsageForRange(packageName: String, startDate: String, endDate: String): List<DailyUsage>
    suspend fun getAllUsageForRange(startDate: String, endDate: String): List<DailyUsage>
    suspend fun deleteOlderThan(beforeDate: String)
}
