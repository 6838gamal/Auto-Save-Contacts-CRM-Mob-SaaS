package gamalsolutions.gamalprojects.autosavecontactscrm.utils

import android.content.ContentProviderOperation
import android.content.Context
import android.provider.ContactsContract
import android.util.Log

object SystemContactSaver {
    
    /**
     * Tries to save a contact name and phone number to the system's local address book.
     * Returns true if successful, false otherwise.
     */
    fun saveToSystem(context: Context, name: String, phoneNumber: String): Boolean {
        val ops = ArrayList<ContentProviderOperation>()
        
        // Step 1: Add empty RawContact
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        
        // Step 2: Add Display Name
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build()
        )
        
        // Step 3: Add Phone Number
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )
        
        return try {
            val results = context.contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            results.isNotEmpty()
        } catch (e: Exception) {
            Log.e("SystemContactSaver", "Error saving system contact: ${e.message}", e)
            false
        }
    }
}
