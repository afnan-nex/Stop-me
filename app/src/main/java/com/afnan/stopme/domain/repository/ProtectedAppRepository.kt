package com.afnan.stopme.domain.repository

import com.afnan.stopme.domain.model.ProtectedApp
import kotlinx.coroutines.flow.Flow

interface ProtectedAppRepository {
    fun observeAll(): Flow<List<ProtectedApp>>
    suspend fun getAll(): List<ProtectedApp>
    suspend fun getByPackageName(packageName: String): ProtectedApp?
    suspend fun add(packageName: String, displayName: String?): Boolean
    suspend fun remove(packageName: String)
    suspend fun isProtected(packageName: String): Boolean
    fun observeEnabledPackageNames(): Flow<List<String>>
    suspend fun getEnabledPackageNames(): List<String>
}
