package utils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
/**
 * Enthält Hilfsfunktionen für die Verarbeitung von Datumswerten.
 */
object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    /**
     * Konvertiert einen Datums-String in ein `LocalDate`-Objekt.
     * @param dateString Der Datums-String im Format "yyyy-MM-dd".
     * @return Ein `LocalDate`-Objekt.
     */
    fun parseDate(dateString: String): LocalDate {
        return LocalDate.parse(dateString, formatter)
    }

    /**
     * Konvertiert ein `LocalDate`-Objekt in einen Datums-String.
     * @param date Das `LocalDate`-Objekt.
     * @return Ein Datums-String im Format "yyyy-MM-dd".
     */
    fun formatDate(date: LocalDate): String {
        return date.format(formatter)
    }
}