package com.afnan.stopme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.afnan.stopme.data.local.entities.DailyUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyUsageDao {

    /**
     * Inserts a new usage record or ignores if one already exists (use [addUsedMillis] to update).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(entity: DailyUsageEntity)

    /**
     * Atomically increments [usedMillis] for a given package+date.
     * If no row exists yet, this is a no-op — call [insertOrIgnore] first.
     */
    @Query("""
        UPDATE daily_usage 
        SET usedMillis = usedMillis + :deltaMillis
        WHERE packageName = :packageName AND localDate = :localDate
    """)
    suspend fun addUsedMillis(packageName: String, localDate: String, deltaMillis: Long)

    /**
     * Atomically adds extra unlocked time for a given package+date.
     */
    @Query("""
        UPDATE daily_usage 
        SET extraUnlockedMillis = extraUnlockedMillis + :extraMillis
        WHERE packageName = :packageName AND localDate = :localDate
    """)
    suspend fun addExtraUnlockedMillis(packageName: String, localDate: String, extraMillis: Long)

    /**
     * Resets usage and extra unlocked time back to 0 for a given package+date.
     */
    @Query("""
        UPDATE daily_usage 
        SET usedMillis = 0, extraUnlockedMillis = 0
        WHERE packageName = :packageName AND localDate = :localDate
    """)
    suspend fun resetUsage(packageName: String, localDate: String)

    @Query("SELECT * FROM daily_usage WHERE packageName = :packageName AND localDate = :localDate LIMIT 1")
    fun observeUsage(packageName: String, localDate: String): Flow<DailyUsageEntity?>

    @Query("SELECT * FROM daily_usage WHERE packageName = :packageName AND localDate = :localDate LIMIT 1")
    suspend fun getUsage(packageName: String, localDate: String): DailyUsageEntity?

    @Query("SELECT * FROM daily_usage WHERE localDate = :localDate")
    fun observeAllUsageForDate(localDate: String): Flow<List<DailyUsageEntity>>

    @Query("SELECT * FROM daily_usage WHERE localDate = :localDate")
    suspend fun getAllUsageForDate(localDate: String): List<DailyUsageEntity>

    @Query("""
        SELECT * FROM daily_usage 
        WHERE packageName = :packageName 
        AND localDate >= :startDate 
        AND localDate <= :endDate
        ORDER BY localDate ASC
    """)
    suspend fun getUsageForRange(
        packageName: String,
        startDate: String,
        endDate: String
    ): List<DailyUsageEntity>

    @Query("""
        SELECT * FROM daily_usage 
        WHERE localDate >= :startDate 
        AND localDate <= :endDate
        ORDER BY localDate ASC
    """)
    suspend fun getAllUsageForRange(startDate: String, endDate: String): List<DailyUsageEntity>

    /** Deletes records older than [beforeDate] to prevent unbounded DB growth. */
    @Query("DELETE FROM daily_usage WHERE localDate < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)

    /** Full export for backup */
    @Query("SELECT * FROM daily_usage ORDER BY localDate DESC")
    suspend fun getAllUsage(): List<DailyUsageEntity>
}
