package gamalsolutions.gamalprojects.autosavecontactscrm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import gamalsolutions.gamalprojects.autosavecontactscrm.services.CRMForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot Completed intercepted! Relaying background monitoring service trigger.")
            context?.let { ctx ->
                CRMForegroundService.startService(ctx)
            }
        }
    }
}
