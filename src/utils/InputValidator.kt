package utils

/**
 * Enthält Hilfsfunktionen zur Validierung von Benutzereingaben.
 */
object InputValidator {
    /**
     * Prüft, ob die angegebene Priorität gültig ist.
     * @param priority Die zu prüfende Priorität.
     * @return `true`, wenn die Priorität gültig ist, sonst `false`.
     */
    fun isValidPriority(priority: Enum<Priority>): Boolean {
        return priority in listOf(
            Priority.PRIORITY_HIGH,
            Priority.PRIORITY_MEDIUM,
            Priority.PRIORITY_LOW
        )
    }

    /**
     * Prüft, ob der angegebene Datums-String ein gültiges Datum ist.
     * @param dateString Der Datums-String.
     * @return `true`, wenn das Datum gültig ist, sonst `false`.
     */
    fun isValidDate(dateString: String): Boolean {
        return try {
            DateUtils.parseDate(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isValidStatus(status:String){

    }
}