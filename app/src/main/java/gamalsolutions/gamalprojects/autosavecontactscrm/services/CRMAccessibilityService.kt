package gamalsolutions.gamalprojects.autosavecontactscrm.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import gamalsolutions.gamalprojects.autosavecontactscrm.database.AppDatabase
import gamalsolutions.gamalprojects.autosavecontactscrm.repositories.CRMRepository
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CRMAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var repository: CRMRepository? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            repository = CRMRepository(applicationContext, db.crmDao())
        } catch (e: Exception) {
            Log.e("CRMAccessibility", "Error initializing database: ${e.message}", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Scan nodes recursively on screen change
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            val rootNode = rootInActiveWindow ?: return
            scanNodeForPhoneNumbers(rootNode)
        }
    }

    override fun onInterrupt() {
        Log.d("CRMAccessibility", "Accessibility service interrupted.")
    }

    private fun scanNodeForPhoneNumbers(node: AccessibilityNodeInfo) {
        val text = node.text?.toString() ?: ""
        if (text.isNotEmpty()) {
            val processedPhone = extractPhone(text)
            if (processedPhone.isNotEmpty() && PhoneNumberUtils.isValidNumber(processedPhone)) {
                serviceScope.launch {
                    try {
                        repository?.processInterceptedNumber(
                            rawNumber = processedPhone,
                            source = "WHATSAPP", // Treat screen scrapes mostly as WhatsApp or general CRM captures
                            details = "Accessibility system scrape from package: ${node.packageName ?: "unknown"}"
                        )
                    } catch (e: Exception) {
                        Log.e("CRMAccessibility", "Error autosaving: ${e.message}", e)
                    }
                }
            }
        }
        
        // Recursive loop info children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scanNodeForPhoneNumbers(child)
        }
    }

    private fun extractPhone(input: String): String {
        val clean = input.filter { it.isDigit() || it == '+' }.trim()
        if (clean.length in 8..15) {
            return clean
        }
        return ""
    }
}
