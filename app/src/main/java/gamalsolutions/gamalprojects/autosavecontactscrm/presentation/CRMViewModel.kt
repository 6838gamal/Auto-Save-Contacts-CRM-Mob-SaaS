package gamalsolutions.gamalprojects.autosavecontactscrm.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import gamalsolutions.gamalprojects.autosavecontactscrm.database.AppDatabase
import gamalsolutions.gamalprojects.autosavecontactscrm.database.ContactEntity
import gamalsolutions.gamalprojects.autosavecontactscrm.database.LogEntity
import gamalsolutions.gamalprojects.autosavecontactscrm.managers.PermissionManager
import gamalsolutions.gamalprojects.autosavecontactscrm.repositories.CRMRepository
import gamalsolutions.gamalprojects.autosavecontactscrm.services.CRMForegroundService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CRMViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CRMRepository(application, db.crmDao())

    // UI state flows
    val contacts: StateFlow<List<ContactEntity>> = repository.contactsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<LogEntity>> = repository.logsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Foreground Service Status
    private val _isServiceRunning = MutableStateFlow(CRMForegroundService.isServiceRunningValue)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    // Permission States
    private val _hasPermissions = MutableStateFlow(PermissionManager.hasRuntimePermissions(application))
    val hasPermissions: StateFlow<Boolean> = _hasPermissions.asStateFlow()

    private val _hasNotificationAccess = MutableStateFlow(PermissionManager.isNotificationAccessGranted(application))
    val hasNotificationAccess: StateFlow<Boolean> = _hasNotificationAccess.asStateFlow()

    private val _hasAccessibility = MutableStateFlow(PermissionManager.isAccessibilityServiceEnabled(application))
    val hasAccessibility: StateFlow<Boolean> = _hasAccessibility.asStateFlow()

    private val _isBatteryOptimizationIgnored = MutableStateFlow(PermissionManager.isBatteryOptimizationIgnored(application))
    val isBatteryOptimizationIgnored: StateFlow<Boolean> = _isBatteryOptimizationIgnored.asStateFlow()

    // Settings States
    private val _autoSaveCalls = MutableStateFlow(true)
    val autoSaveCalls: StateFlow<Boolean> = _autoSaveCalls.asStateFlow()

    private val _autoSaveSms = MutableStateFlow(true)
    val autoSaveSms: StateFlow<Boolean> = _autoSaveSms.asStateFlow()

    private val _autoSaveWhatsApp = MutableStateFlow(true)
    val autoSaveWhatsApp: StateFlow<Boolean> = _autoSaveWhatsApp.asStateFlow()

    private val _namePrefix = MutableStateFlow("Customer")
    val namePrefix: StateFlow<String> = _namePrefix.asStateFlow()

    private val _appTheme = MutableStateFlow("system")
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    private val _userGmail = MutableStateFlow<String?>(null)
    val userGmail: StateFlow<String?> = _userGmail.asStateFlow()

    private val _autoGmailSync = MutableStateFlow(true)
    val autoGmailSync: StateFlow<Boolean> = _autoGmailSync.asStateFlow()

    init {
        refreshIntegrationStatuses()
        loadSettings()
    }

    fun refreshIntegrationStatuses() {
        val app = getApplication<Application>()
        _isServiceRunning.value = CRMForegroundService.isServiceRunningValue
        _hasPermissions.value = PermissionManager.hasRuntimePermissions(app)
        _hasNotificationAccess.value = PermissionManager.isNotificationAccessGranted(app)
        _hasAccessibility.value = PermissionManager.isAccessibilityServiceEnabled(app)
        _isBatteryOptimizationIgnored.value = PermissionManager.isBatteryOptimizationIgnored(app)
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _autoSaveCalls.value = repository.getSetting("auto_save_calls", "true") == "true"
            _autoSaveSms.value = repository.getSetting("auto_save_sms", "true") == "true"
            _autoSaveWhatsApp.value = repository.getSetting("auto_save_whatsapp", "true") == "true"
            _namePrefix.value = repository.getSetting("name_prefix", "Customer")
            _appTheme.value = repository.getSetting("app_theme", "system")
            val email = repository.getSetting("user_gmail", "")
            _userGmail.value = if (email.isEmpty()) null else email
            _autoGmailSync.value = repository.getSetting("auto_gmail_sync", "true") == "true"
        }
    }

    fun toggleAutoSaveCalls(enabled: Boolean) {
        _autoSaveCalls.value = enabled
        viewModelScope.launch { repository.saveSetting("auto_save_calls", enabled.toString()) }
    }

    fun toggleAutoSaveSms(enabled: Boolean) {
        _autoSaveSms.value = enabled
        viewModelScope.launch { repository.saveSetting("auto_save_sms", enabled.toString()) }
    }

    fun toggleAutoSaveWhatsApp(enabled: Boolean) {
        _autoSaveWhatsApp.value = enabled
        viewModelScope.launch { repository.saveSetting("auto_save_whatsapp", enabled.toString()) }
    }

    fun updateNamePrefix(prefix: String) {
        _namePrefix.value = prefix
        viewModelScope.launch { repository.saveSetting("name_prefix", prefix) }
    }

    fun updateAppTheme(theme: String) {
        _appTheme.value = theme
        viewModelScope.launch { repository.saveSetting("app_theme", theme) }
    }

    fun updateUserGmail(email: String) {
        _userGmail.value = if (email.trim().isEmpty()) null else email.trim()
        viewModelScope.launch { repository.saveSetting("user_gmail", email.trim()) }
    }

    fun toggleAutoGmailSync(enabled: Boolean) {
        _autoGmailSync.value = enabled
        viewModelScope.launch { repository.saveSetting("auto_gmail_sync", enabled.toString()) }
    }

    fun importDeviceContacts(onResult: (Int, Int) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    app,
                    android.Manifest.permission.READ_CONTACTS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                onResult(0, 0)
                return@launch
            }

            var imported = 0
            var skipped = 0
            val uri = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val projection = arrayOf(
                android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
            )

            val systemContactList = mutableListOf<Pair<String, String>>()
            try {
                app.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val numIdx = cursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (cursor.moveToNext()) {
                        val name = if (nameIdx >= 0) cursor.getString(nameIdx) ?: "" else ""
                        val num = if (numIdx >= 0) cursor.getString(numIdx) ?: "" else ""
                        if (num.isNotBlank()) {
                            systemContactList.add(Pair(name, num))
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("CRMViewModel", "Error scanning system contacts: ${e.message}")
            }

            // For each contact, standardize it
            for ((name, num) in systemContactList) {
                val cleanPhone = gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils.formatNumber(num)
                if (gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils.isValidNumber(cleanPhone)) {
                    // Check if it exists in local CRM database
                    val existing = repository.getAllContactsList().firstOrNull { it.phoneNumber == cleanPhone }
                    if (existing == null) {
                        val timestamp = System.currentTimeMillis()
                        val importedContact = gamalsolutions.gamalprojects.autosavecontactscrm.database.ContactEntity(
                            name = if (name.isNotBlank()) name else "${_namePrefix.value} $cleanPhone",
                            phoneNumber = cleanPhone,
                            source = "SYSTEM_IMPORT",
                            savedTimestamp = timestamp,
                            lastInteractionTimestamp = timestamp,
                            interactionCount = 1
                        )
                        db.crmDao().insertContact(importedContact)
                        
                        // Insert a log
                        db.crmDao().insertLog(
                            gamalsolutions.gamalprojects.autosavecontactscrm.database.LogEntity(
                                phoneNumber = cleanPhone,
                                name = importedContact.name,
                                source = "SYSTEM_IMPORT",
                                status = "SUCCESS",
                                timestamp = timestamp,
                                details = "Manually imported system contact into local CRM index safely."
                            )
                        )
                        imported++
                    } else {
                        skipped++
                    }
                } else {
                    skipped++
                }
            }
            onResult(imported, skipped)
        }
    }

    fun addContactManually(name: String, phone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val trimmedName = name.trim()
            val cleanPhone = gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils.formatNumber(phone)
            
            if (trimmedName.isEmpty()) {
                onResult(false, "من فضلك أدخل اسماً صحيحاً للعميل")
                return@launch
            }
            
            if (!gamalsolutions.gamalprojects.autosavecontactscrm.utils.PhoneNumberUtils.isValidNumber(cleanPhone)) {
                onResult(false, "رقم الهاتف غير صحيح، يجب أن يتكون من 7 إلى 15 رقماً")
                return@launch
            }

            // Check duplicate in local CRM
            val existingLocal = db.crmDao().getContactByPhone(cleanPhone)
            if (existingLocal != null) {
                onResult(false, "هذا الرقم مسجل مسبقاً في التطبيق باسم: ${existingLocal.name}")
                return@launch
            }

            // Check duplicate in system contacts
            val existsInSystem = gamalsolutions.gamalprojects.autosavecontactscrm.utils.ContactExistenceChecker.existsInSystemContacts(app, cleanPhone)
            if (existsInSystem) {
                onResult(false, "هذا الرقم مسجل بالفعل في جهات اتصال الهاتف")
                return@launch
            }

            val timestamp = System.currentTimeMillis()
            
            // Try saving to system contacts first to maintain sync
            val savedToSystem = gamalsolutions.gamalprojects.autosavecontactscrm.utils.SystemContactSaver.saveToSystem(app, trimmedName, cleanPhone)
            
            if (savedToSystem) {
                // Save to CRM local db
                db.crmDao().insertContact(
                    gamalsolutions.gamalprojects.autosavecontactscrm.database.ContactEntity(
                        name = trimmedName,
                        phoneNumber = cleanPhone,
                        source = "MANUAL",
                        savedTimestamp = timestamp,
                        lastInteractionTimestamp = timestamp,
                        interactionCount = 1
                    )
                )

                var logDetails = "حفظ يدوي ناجح للعميل '$trimmedName'."
                val gmail = _userGmail.value
                if (_autoGmailSync.value && !gmail.isNullOrBlank()) {
                    logDetails += " تمت المزامنة مع الحساب السحابي ($gmail) تلقائياً."
                }

                db.crmDao().insertLog(
                    gamalsolutions.gamalprojects.autosavecontactscrm.database.LogEntity(
                        phoneNumber = cleanPhone,
                        name = trimmedName,
                        source = "MANUAL",
                        status = "SUCCESS",
                        timestamp = timestamp,
                        details = logDetails
                    )
                )
                onResult(true, "تم إضافة جهة الاتصال وحفظها بنجاح في الهاتف والتطبيق")
            } else {
                onResult(false, "فشل حفظ جهة الاتصال في الهاتف، يرجى التحقق من صلاحيات جهات الاتصال")
            }
        }
    }

    fun insertManualBackupLog(details: String) {
        viewModelScope.launch {
            db.crmDao().insertLog(
                gamalsolutions.gamalprojects.autosavecontactscrm.database.LogEntity(
                    phoneNumber = "-",
                    name = "Manual Gmail Backup",
                    source = "EXPORT",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis(),
                    details = details
                )
            )
        }
    }

    fun startService() {
        CRMForegroundService.startService(getApplication())
        _isServiceRunning.value = true
    }

    fun stopService() {
        CRMForegroundService.stopService(getApplication())
        _isServiceRunning.value = false
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    fun updateContactName(contact: ContactEntity, newName: String) {
        viewModelScope.launch {
            repository.updateContactName(contact, newName)
        }
    }

    // Interactive SIMULATOR to easily trigger intercepts for demo testing
    fun simulatePhoneCall(rawNumber: String) {
        viewModelScope.launch {
            repository.processInterceptedNumber(
                rawNumber = rawNumber,
                source = "CALL",
                details = "User-triggered Interactive Simulation Console action."
            )
        }
    }

    fun simulateWhatsAppMessage(rawNumber: String, customSenderName: String) {
        viewModelScope.launch {
            repository.processInterceptedNumber(
                rawNumber = rawNumber,
                source = "WHATSAPP",
                details = "WhatsApp notification simulation: sender: '$customSenderName'"
            )
        }
    }
}
