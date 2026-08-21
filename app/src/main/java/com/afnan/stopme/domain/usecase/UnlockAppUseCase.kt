package com.afnan.stopme.domain.usecase

import com.afnan.stopme.core.common.utils.DAILY_LIMIT_MILLIS
import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import com.afnan.stopme.domain.repository.UsageRepository
import javax.inject.Inject

class UnlockAppUseCase @Inject constructor(
    private val usageRepo: UsageRepository
) {
    /**
     * Grants [DAILY_LIMIT_MILLIS] (+30 minutes) of extra time to [packageName] for today.
     * Each successful challenge call adds exactly one increment.
     */
    suspend operator fun invoke(packageName: String) {
        val today = todayLocalDate().toDateString()
        usageRepo.addExtraUnlockedMillis(packageName, today, DAILY_LIMIT_MILLIS)
    }
}
