package com.afnan.stopme.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.afnan.stopme.data.local.dao.DailyUsageDao
import com.afnan.stopme.data.local.dao.ProtectedAppDao
import com.afnan.stopme.data.local.entities.DailyUsageEntity
import com.afnan.stopme.data.local.entities.ProtectedAppEntity

@Database(
    entities = [
        ProtectedAppEntity::class,
        DailyUsageEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StopMeDatabase : RoomDatabase() {
    abstract fun protectedAppDao(): ProtectedAppDao
    abstract fun dailyUsageDao(): DailyUsageDao
}
