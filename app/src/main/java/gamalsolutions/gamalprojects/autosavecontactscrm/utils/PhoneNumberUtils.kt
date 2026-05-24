package gamalsolutions.gamalprojects.autosavecontactscrm.utils

object PhoneNumberUtils {
    /**
     * Standardizes a phone number by stripping out non-digit characters, 
     * except for a leading plus sign.
     */
    fun formatNumber(rawNumber: String): String {
        val trimmed = rawNumber.trim()
        if (trimmed.isEmpty()) return ""
        
        val isPlus = trimmed.startsWith("+")
        val cleanDigits = trimmed.filter { it.isDigit() }
        
        return if (isPlus) "+$cleanDigits" else cleanDigits
    }
    
    /**
     * Checks if the number is valid enough to be treated as a customer number.
     */
    fun isValidNumber(phone: String): Boolean {
        val formatted = formatNumber(phone)
        // Usually, a valid phone number digit count is between 7 and 15 digits
        val digitOnly = formatted.filter { it.isDigit() }
        return digitOnly.length in 7..15
    }
}
