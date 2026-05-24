package gamalsolutions.gamalprojects.autosavecontactscrm.repositories

import android.content.Context
import android.util.Log
import gamalsolutions.gamalprojects.autosavecontactscrm.database.*
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.ContactExistenceChecker
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils
import gamalsolutions.gamalprojects.autosavecontactscrm.utils.SystemContactSaver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.util.Date

class CRMRepository(private val context: Context, private val dao: CRMDao) {

    val contactsFlow: Flow<List<ContactEntity>> = dao.getAllContactsFlow().flowOn(Dispatchers.IO)
    val logsFlow: Flow<List<LogEntity>> = dao.getAllLogsFlow().flowOn(Dispatchers.IO)

    suspend fun getAllContactsList(): List<ContactEntity> = withContext(Dispatchers.IO) {
        dao.getAllContacts()
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        dao.clearAllLogs()
    }

    suspend fun deleteContact(contact: ContactEntity) = withContext(Dispatchers.IO) {
        dao.deleteContact(contact)
    }

    suspend fun updateContactName(contact: ContactEntity, newName: String) = withContext(Dispatchers.IO) {
        // Update both CRM Local Database and optionally we could update system contacts.
        val updated = contact.copy(name = newName)
        dao.updateContact(updated)
    }

    // Settings helpers
    suspend fun getSetting(key: String, defaultValue: String): String = withContext(Dispatchers.IO) {
        dao.getSetting(key)?.value ?: defaultValue
    }

    suspend fun saveSetting(key: String, value: String) = withContext(Dispatchers.IO) {
        dao.insertSetting(SettingEntity(key, value))
    }

    /**
     * Intercepts and parses a phone number from calls, messages, or WhatsApp notifications,
     * running verification checks, then auto-saving if qualifying.
     */
    suspend fun processInterceptedNumber(
        rawNumber: String,
        source: String,
        details: String
    ): Boolean = withContext(Dispatchers.IO) {
        val cleanNumber = PhoneNumberUtils.formatNumber(rawNumber)
        if (!PhoneNumberUtils.isValidNumber(cleanNumber)) {
            Log.d("CRMRepository", "Ignored: Number formatted to '$cleanNumber' is invalid.")
            return@withContext false
        }

        // 1. Check if auto-save for this channel is enabled in settings
        val settingKey = when (source) {
            "CALL" -> "auto_save_calls"
            "SMS" -> "auto_save_sms"
            "WHATSAPP" -> "auto_save_whatsapp"
            else -> "auto_save_generic"
        }
        val isEnabledStr = getSetting(settingKey, "true")
        if (isEnabledStr == "false") {
            Log.d("CRMRepository", "Ignored: Interceptor for $source is disabled in settings.")
            return@withContext false
        }

        // 2. Check system address book
        val existsInSystem = ContactExistenceChecker.existsInSystemContacts(context, cleanNumber)
        
        // 3. Check CRM Local Database
        val existingCrmContact = dao.getContactByPhone(cleanNumber)

        val timestamp = System.currentTimeMillis()

        if (existsInSystem || existingCrmContact != null) {
            // Already saved! Let's update interaction stats in CRM database if it exists
            if (existingCrmContact != null) {
                val updatedContact = existingCrmContact.copy(
                    lastInteractionTimestamp = timestamp,
                    interactionCount = existingCrmContact.interactionCount + 1
                )
                dao.updateContact(updatedContact)
                
                // Write detailed operational log
                dao.insertLog(
                    LogEntity(
                        phoneNumber = cleanNumber,
                        name = existingCrmContact.name,
                        source = source,
                        status = "IGNORED",
                        timestamp = timestamp,
                        details = "No save needed. Already exists in system contacts or local log database."
                    )
                )
            } else {
                // If it exists in system contacts only, import details to CRM database
                val defaultPrefix = getSetting("name_prefix", "Customer")
                val importedName = "$defaultPrefix $cleanNumber"
                dao.insertContact(
                    ContactEntity(
                        name = importedName,
                        phoneNumber = cleanNumber,
                        source = source,
                        savedTimestamp = timestamp,
                        lastInteractionTimestamp = timestamp,
                        interactionCount = 1
                    )
                )
                dao.insertLog(
                    LogEntity(
                        phoneNumber = cleanNumber,
                        name = importedName,
                        source = source,
                        status = "IGNORED",
                        timestamp = timestamp,
                        details = "Already saved in phone system index. Registered as imported client."
                    )
                )
            }
            return@withContext false
        }

        // 4. Truly unsaved target detected! Auto-Save!
        val defaultPrefix = getSetting("name_prefix", "Customer")
        val generatedName = "$defaultPrefix $cleanNumber"

        // Save to system address book
        val savedToSystem = SystemContactSaver.saveToSystem(context, generatedName, cleanNumber)

        if (savedToSystem) {
            // Register to CRM Local Database
            dao.insertContact(
                ContactEntity(
                    name = generatedName,
                    phoneNumber = cleanNumber,
                    source = source,
                    savedTimestamp = timestamp,
                    lastInteractionTimestamp = timestamp,
                    interactionCount = 1
                )
            )

            // Insert system logs
            dao.insertLog(
                LogEntity(
                    phoneNumber = cleanNumber,
                    name = generatedName,
                    source = source,
                    status = "SUCCESS",
                    timestamp = timestamp,
                    details = "Auto-save succeeded for $source. Added name: '$generatedName' to local phone dictionary."
                )
            )
            return@withContext true
        } else {
            // System save failed (might be permissions etc.)
            dao.insertLog(
                LogEntity(
                    phoneNumber = cleanNumber,
                    name = generatedName,
                    source = source,
                    status = "FAILED",
                    timestamp = timestamp,
                    details = "Failed to write contact data into system address book. Ensure permissions are active."
                )
            )
            return@withContext false
        }
    }
}
