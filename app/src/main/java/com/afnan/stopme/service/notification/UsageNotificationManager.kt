package com.afnan.stopme.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.afnan.stopme.MainActivity
import com.afnan.stopme.R
import com.afnan.stopme.core.common.utils.toMinuteSecondString
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages silent usage notification displaying the currently tracked protected app and its usage.
 *
 * Line 1: <Application Name> (e.g. "Instagram")
 * Line 2: Minutes used (MM:SS) (e.g. "Minutes used (10:24)")
 *
 * The notification only appears when a protected app is in the foreground,
 * and is immediately dismissed when leaving the app or idle.
 */
@Singleton
class UsageNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val TRACKING_CHANNEL_ID = "stop_me_active_tracking"
        const val NOTIFICATION_ID = 1001
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            TRACKING_CHANNEL_ID,
            "Active Usage Tracking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows real-time usage while a protected app is open."
            setShowBadge(false)
            enableVibration(false)
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Updates notification with active tracking information:
     * Line 1: [appLabel]
     * Line 2: Minutes used (MM:SS)
     */
    fun updateTrackingNotification(appLabel: String, usedMillis: Long) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = usedMillis.toMinuteSecondString()
        val builder = NotificationCompat.Builder(context, TRACKING_CHANNEL_ID)
            .setContentTitle(appLabel)
            .setContentText("Minutes used ($formattedTime)")
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(ContextCompat.getColor(context, R.color.notification_accent))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
        }

        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: Exception) {
            // Ignore if notification permission is not yet granted
        }
    }

    /**
     * Cancels and dismisses the tracking notification when leaving a protected app.
     */
    fun cancelTrackingNotification() {
        try {
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
