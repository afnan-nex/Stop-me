package com.afnan.stopme.domain.usecase

import com.afnan.stopme.core.common.utils.InstalledAppsHelper
import com.afnan.stopme.domain.repository.ProtectedAppRepository
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

sealed class AddAppResult {
    object Success : AddAppResult()
    object Duplicate : AddAppResult()
    object InvalidPackage : AddAppResult()
}

class AddProtectedAppUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ProtectedAppRepository
) {
    suspend operator fun invoke(packageName: String): AddAppResult {
        val trimmed = packageName.trim()
        if (!InstalledAppsHelper.isValidPackageName(trimmed)) {
            return AddAppResult.InvalidPackage
        }
        val displayName = InstalledAppsHelper.getAppLabel(context, trimmed)
        val added = repository.add(trimmed, displayName)
        return if (added) AddAppResult.Success else AddAppResult.Duplicate
    }
}
