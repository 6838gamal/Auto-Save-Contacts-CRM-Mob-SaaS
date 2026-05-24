package gamalsolutions.gamalprojects.autosavecontactscrm.managers

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager
import androidx.core.content.ContextCompat
import gamalsolutions.gamalprojects.autosavecontactscrm.services.CRMAccessibilityService

object PermissionManager {

    /**
     * Set of basic manifest permissions needed for standard SMS & call intercept
     */
    val REQUIRED_PERMISSIONS: List<String> = buildList {
        add(android.Manifest.permission.READ_CONTACTS)
        add(android.Manifest.permission.WRITE_CONTACTS)
        add(android.Manifest.permission.READ_CALL_LOG)
        add(android.Manifest.permission.READ_PHONE_STATE)
        add(android.Manifest.permission.RECEIVE_SMS)
        add(android.Manifest.permission.READ_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Checks if all basic runtime permissions are granted.
     */
    fun hasRuntimePermissions(context: Context): Boolean {
        return REQUIRED_PERMISSIONS.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if notification listener service is authorized on system settings.
     */
    fun isNotificationAccessGranted(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!flat.isNullOrEmpty()) {
            val names = flat.split(":")
            for (name in names) {
                if (name.contains(packageName)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if our Accessibility Service is enabled.
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedComponentName = "${context.packageName}/${CRMAccessibilityService::class.java.canonicalName}"
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        for (service in enabledServices) {
            if (service.id == expectedComponentName) {
                return true
            }
        }
        // Fallback method
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return settingValue?.contains(context.packageName) == true
    }

    /**
     * Checks if the app is excluded from battery optimizations.
     */
    fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * Opens notification listener setting.
     */
    fun getNotificationSettingsIntent(): Intent {
        return Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
    }

    /**
     * Opens battery settings.
     */
    @SuppressLint("BatteryLife")
    fun getBatteryOptimizationSettingsIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * Opens Accessibility Settings page.
     */
    fun getAccessibilitySettingsIntent(): Intent {
        return Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    }
}
