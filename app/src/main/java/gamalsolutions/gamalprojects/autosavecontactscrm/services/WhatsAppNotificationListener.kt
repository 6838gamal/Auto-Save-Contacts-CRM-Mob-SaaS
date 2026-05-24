package gamalsolutions.gamalprojects.autosavecontactscrm.services

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import gamalsolutions.gamalprojects.autosavecontactscrm.database.AppDatabase
import gamalsolutions.gamalprojects.autosavecontactscrm.repositories.CRMRepository
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WhatsAppNotificationListener : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var repository: CRMRepository? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            repository = CRMRepository(applicationContext, db.crmDao())
        } catch (e: Exception) {
            Log.e("WhatsAppListener", "Error initializing repository: ${e.message}", e)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        val packageName = sbn.packageName
        // Intercept WhatsApp or WhatsApp Business notifications
        if (packageName == "com.whatsapp" || packageName == "com.whatsapp.w4b") {
            val extras = sbn.notification?.extras ?: return
            val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

            Log.d("WhatsAppListener", "Notification Intercepted from $packageName - Title: '$title', Text: '$text'")

            // Seek clean numbers inside notification title first (unregistered WhatsApp users usually display their raw number as title)
            val phoneFromTitle = extractPhoneNumber(title)
            val phoneFromText = extractPhoneNumber(text)

            val finalPhone = if (phoneFromTitle.isNotEmpty()) phoneFromTitle else phoneFromText

            if (finalPhone.isNotEmpty()) {
                serviceScope.launch {
                    try {
                        val saved = repository?.processInterceptedNumber(
                            rawNumber = finalPhone,
                            source = "WHATSAPP",
                            details = "Notification title: '$title'\nMessage preview: '$text'"
                        ) ?: false
                        if (saved) {
                            Log.d("WhatsAppListener", "WhatsApp capture completed for: $finalPhone")
                        }
                    } catch (e: Exception) {
                        Log.e("WhatsAppListener", "Error executing save loop: ${e.message}", e)
                    }
                }
            }
        }
    }

    private fun extractPhoneNumber(input: String): String {
        if (input.isEmpty()) return ""
        
        // Match numbers like +967 774 440 982 or +967774440982 or 774440982
        // Match standard digits patterns
        val cleanInput = input.trim()
        
        // If the whole string matches standard phone format (with or without plus, spaces, dashes)
        val digitAndPlusPattern = Regex("[+]?[0-9\\s\\-]{7,18}")
        val matches = digitAndPlusPattern.findAll(cleanInput)
        
        for (match in matches) {
            val possiblePhone = match.value.filter { it.isDigit() || it == '+' }.trim()
            if (PhoneNumberUtils.isValidNumber(possiblePhone)) {
                return possiblePhone
            }
        }
        
        return ""
    }
}
