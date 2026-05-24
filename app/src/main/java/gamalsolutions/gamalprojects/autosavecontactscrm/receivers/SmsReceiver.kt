package gamalsolutions.gamalprojects.autosavecontactscrm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import gamalsolutions.gamalprojects.autosavecontactscrm.database.AppDatabase
import gamalsolutions.gamalprojects.autosavecontactscrm.repositories.CRMRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
            context?.let { ctx ->
                val db = AppDatabase.getDatabase(ctx)
                val repository = CRMRepository(ctx, db.crmDao())
                
                for (sms in messages) {
                    val incomingNumber = sms.originatingAddress ?: continue
                    val body = sms.messageBody ?: ""
                    
                    Log.d("SmsReceiver", "Intercepted SMS message from sender: $incomingNumber")
                    
                    receiverScope.launch {
                        try {
                            repository.processInterceptedNumber(
                                rawNumber = incomingNumber,
                                source = "SMS",
                                details = "SMS body preview: '${body.take(60)}...'"
                            )
                        } catch (e: Exception) {
                            Log.e("SmsReceiver", "Error saving SMS sender: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }
}
