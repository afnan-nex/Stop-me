package com.afnan.stopme.data.repository

import com.afnan.stopme.data.local.dao.ProtectedAppDao
import com.afnan.stopme.data.local.entities.ProtectedAppEntity
import com.afnan.stopme.domain.model.ProtectedApp
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProtectedAppRepositoryImpl @Inject constructor(
    private val dao: ProtectedAppDao
) : ProtectedAppRepository {

    override fun observeAll(): Flow<List<ProtectedApp>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getAll(): List<ProtectedApp> =
        dao.getAll().map { it.toDomain() }

    override suspend fun getByPackageName(packageName: String): ProtectedApp? =
        dao.getByPackageName(packageName)?.toDomain()

    /**
     * Inserts the app. Returns false if the package is already in the DB (duplicate).
     */
    override suspend fun add(packageName: String, displayName: String?): Boolean {
        val inserted = dao.insertApp(
            ProtectedAppEntity(
                packageName = packageName,
                displayName = displayName,
                addedAt = System.currentTimeMillis(),
                enabled = true
            )
        )
        return inserted != -1L // -1 means IGNORE triggered (duplicate)
    }

    override suspend fun remove(packageName: String) {
        dao.deleteApp(packageName)
    }

    override suspend fun isProtected(packageName: String): Boolean =
        dao.isProtected(packageName)

    override fun observeEnabledPackageNames(): Flow<List<String>> =
        dao.observeEnabledPackageNames()

    override suspend fun getEnabledPackageNames(): List<String> =
        dao.getEnabledPackageNames()

    private fun ProtectedAppEntity.toDomain() = ProtectedApp(
        packageName = packageName,
        displayName = displayName,
        addedAt = addedAt,
        enabled = enabled
    )
}
