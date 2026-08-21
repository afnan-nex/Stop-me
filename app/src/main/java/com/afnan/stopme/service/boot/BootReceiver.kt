package com.afnan.stopme.service.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.afnan.stopme.service.usage.UsageReconciliationWorker
import dagger.hilt.android.AndroidEntryPoint

/**
 * Receives BOOT_COMPLETED to reschedule WorkManager jobs after reboot.
 * The AccessibilityService is re-enabled by Android automatically if the user
 * had it enabled before the reboot.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            // Reschedule periodic reconciliation worker
            UsageReconciliationWorker.schedule(context)
        }
    }
}
