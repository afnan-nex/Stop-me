package com.afnan.stopme.domain.usecase

import com.afnan.stopme.domain.repository.ProtectedAppRepository
import javax.inject.Inject

class RemoveProtectedAppUseCase @Inject constructor(
    private val repository: ProtectedAppRepository
) {
    /**
     * Removes a protected app. Caller is responsible for gating this behind
     * the unlock challenge before invoking.
     */
    suspend operator fun invoke(packageName: String) {
        repository.remove(packageName)
    }
}
