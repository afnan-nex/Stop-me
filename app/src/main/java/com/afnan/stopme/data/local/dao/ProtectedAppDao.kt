package com.afnan.stopme.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.afnan.stopme.data.local.entities.ProtectedAppEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProtectedAppDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertApp(app: ProtectedAppEntity): Long

    @Query("DELETE FROM protected_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)

    @Query("SELECT * FROM protected_apps ORDER BY addedAt ASC")
    fun observeAll(): Flow<List<ProtectedAppEntity>>

    @Query("SELECT * FROM protected_apps ORDER BY addedAt ASC")
    suspend fun getAll(): List<ProtectedAppEntity>

    @Query("SELECT * FROM protected_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): ProtectedAppEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM protected_apps WHERE packageName = :packageName)")
    suspend fun isProtected(packageName: String): Boolean

    @Query("SELECT packageName FROM protected_apps WHERE enabled = 1")
    fun observeEnabledPackageNames(): Flow<List<String>>

    @Query("SELECT packageName FROM protected_apps WHERE enabled = 1")
    suspend fun getEnabledPackageNames(): List<String>

    @Query("UPDATE protected_apps SET enabled = :enabled WHERE packageName = :packageName")
    suspend fun setEnabled(packageName: String, enabled: Boolean)
}
