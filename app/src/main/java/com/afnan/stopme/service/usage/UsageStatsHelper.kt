package com.afnan.stopme.service.usage

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.afnan.stopme.core.common.utils.todayLocalDate
import com.afnan.stopme.core.common.utils.toDateString
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

data class PackageUsageStat(
    val packageName: String,
    val totalTimeMillis: Long
)

@Singleton
class UsageStatsHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager by lazy {
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    }

    /**
     * Queries today's foreground time for each package in [packageNames].
     * Returns a map of packageName → total foreground milliseconds today.
     */
    fun queryTodayUsage(packageNames: Set<String>): Map<String, Long> {
        val today = todayLocalDate()
        val startMs = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs = System.currentTimeMillis()

        val result = mutableMapOf<String, Long>()

        try {
            val events = usageStatsManager.queryEvents(startMs, endMs)
            val event = UsageEvents.Event()

            val foregroundStartMap = mutableMapOf<String, Long>()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.packageName !in packageNames) continue

                when (event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        foregroundStartMap[event.packageName] = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val start = foregroundStartMap.remove(event.packageName)
                        if (start != null) {
                            val duration = event.timeStamp - start
                            result[event.packageName] = (result[event.packageName] ?: 0L) + duration
                        }
                    }
                }
            }

            // Close any open foreground sessions (app still in foreground)
            foregroundStartMap.forEach { (pkg, start) ->
                val duration = endMs - start
                result[pkg] = (result[pkg] ?: 0L) + duration
            }
        } catch (e: Exception) {
            // Usage access not granted or other error — return empty
        }

        return result
    }
}
