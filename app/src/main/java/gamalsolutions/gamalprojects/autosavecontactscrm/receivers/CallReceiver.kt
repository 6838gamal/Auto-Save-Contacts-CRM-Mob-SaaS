package gamalsolutions.gamalprojects.autosavecontactscrm.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import gamalsolutions.gamalprojects.autosavecontactscrm.database.AppDatabase
import gamalsolutions.gamalprojects.autosavecontactscrm.repositories.CRMRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {

    private val receiverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var savedIncomingNumber: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
            var rawNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            
            val state = when (stateStr) {
                TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
                TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                else -> TelephonyManager.CALL_STATE_IDLE
            }
            
            context?.let { ctx ->
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    if (rawNumber != null) {
                        savedIncomingNumber = rawNumber
                    }
                    Log.d("CallReceiver", "Phone ringing, incoming number detected: $rawNumber")
                } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    Log.d("CallReceiver", "Phone call answered or active.")
                } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                    // Call ended: if we had a ringing number, process it!
                    val finalNumber = rawNumber ?: savedIncomingNumber
                    if (finalNumber != null) {
                        Log.d("CallReceiver", "Phone call idle. Processing call-ended target number: $finalNumber")
                        val db = AppDatabase.getDatabase(ctx)
                        val repository = CRMRepository(ctx, db.crmDao())
                        
                        receiverScope.launch {
                            try {
                                repository.processInterceptedNumber(
                                    rawNumber = finalNumber,
                                    source = "CALL",
                                    details = "Incoming call interception on state transition."
                                )
                            } catch (e: Exception) {
                                Log.e("CallReceiver", "Error saving call contact: ${e.message}", e)
                            } finally {
                                savedIncomingNumber = null
                            }
                        }
                    }
                }
                lastState = state
            }
        }
    }
}
