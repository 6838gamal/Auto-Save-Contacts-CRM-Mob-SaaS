package gamalsolutions.gamalprojects.autosavecontactscrm.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

object ContactExistenceChecker {
    
    /**
     * Queries the system contacts provider to determine if the given phone number already exists.
     */
    fun existsInSystemContacts(context: Context, phoneNumber: String): Boolean {
        if (ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        
        if (phoneNumber.trim().isEmpty()) return false
        
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, 
            Uri.encode(phoneNumber)
        )
        
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)
        
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return true // Number exists in system contacts
            }
        }
        
        return false
    }
}
