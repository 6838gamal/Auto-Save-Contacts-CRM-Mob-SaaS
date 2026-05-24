package gamalsolutions.gamalprojects.autosavecontactscrm.utils

import gamalsolutions.gamalprojects.autosavecontactscrm.database.ContactEntity
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExcelExporter {
    
    /**
     * Converts contacts list to UTF-8 CSV format with Excel BOM (Byte Order Mark).
     */
    fun exportToCsv(contacts: List<ContactEntity>): ByteArray {
        val out = ByteArrayOutputStream()
        
        // Write UTF-8 BOM so Excel opens Arabic correctly
        out.write(0xEF)
        out.write(0xBB)
        out.write(0xBF)
        
        val writer = out.writer(Charsets.UTF_8)
        
        // Header
        writer.write("ID,Name,Phone,Source,Saved Date,Interactions\n")
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        contacts.forEach { contact ->
            val dateStr = dateFormat.format(Date(contact.savedTimestamp))
            val safeName = escapeCsv(contact.name)
            val safePhone = escapeCsv(contact.phoneNumber)
            val safeSource = escapeCsv(contact.source)
            writer.write("${contact.id},$safeName,$safePhone,$safeSource,$dateStr,${contact.interactionCount}\n")
        }
        
        writer.flush()
        return out.toByteArray()
    }
    
    private fun escapeCsv(value: String): String {
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            val cleanValue = value.replace("\"", "\"\"")
            return "\"$cleanValue\""
        }
        return value
    }
}
