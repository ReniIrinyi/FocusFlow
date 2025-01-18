package model

import utils.Priority
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Die `Task`-Datenklasse repräsentiert eine Aufgabe innerhalb des Systems.
 * Jede Aufgabe gehört zu einem Benutzer und enthält alle Details wie Titel, Priorität, Zeiten und Status.
 *
 * @property id Eindeutiger Bezeichner der Aufgabe.
 * @property userId Die ID des Benutzers, dem die Aufgabe zugewiesen ist.
 * @property title Der Titel der Aufgabe.
 * @property priority Die Priorität der Aufgabe (z. B. "High", "Medium", "Low").
 * @property createdAt Das Datum und die Uhrzeit der Erstellung der Aufgabe.
 * @property updatedAt Das Datum und die Uhrzeit der letzten Aktualisierung der Aufgabe.
 * @property startTime Startzeitpunkt der Aufgabe (optional).
 * @property endTime Endzeitpunkt der Aufgabe (optional).
 * @property deadline Fälligkeitsdatum der Aufgabe (optional).
 * @property status Der aktuelle Status der Aufgabe (z. B. "Offen", "Erledigt").
 * @property imageBase64 Ein Bild im Base64-String-Format, das mit der Aufgabe verknüpft ist (z. B. ein Screenshot).
 */
data class Task (
    val id: Int,                         // Eindeutiger Bezeichner der Aufgabe
    val userId: Int,                     // Benutzer-ID, dem die Aufgabe zugewiesen ist
    val title: String,                   // Titel der Aufgabe
    val priority: String,                // Beschreibung der Priorität (z. B. High, Medium, Low)
    val createdAt: LocalDateTime,        // Erstellungsdatum und -zeit der Aufgabe
    var updatedAt: LocalDateTime,        // Letzte Änderungszeit der Aufgabe
    val startTime: LocalDateTime?,       // Startzeit der Aufgabe (null, wenn nicht definiert)
    val endTime: LocalDateTime?,         // Endzeit der Aufgabe (null, wenn nicht definiert)
    val deadline: LocalDateTime?,        // Deadline für die Aufgabe (null, wenn keine Deadline vorhanden ist)
    var status: String,                  // Status der Aufgabe (z. B. Offen, Erledigt)
    val imageBase64: String,             // Optionales Bild im Base64-Format
) {

    companion object {
        var currentId: Int = 0 // Hält die aktuelle ID, die für die nächste Aufgabe verwendet wird.

        /**
         * Generiert eine neue, eindeutige ID für eine Aufgabe.
         *
         * @return Die nächste ID als Ganzzahl.
         */
        fun generateId(): Int {
            currentId++ // Erhöht die aktuelle ID um 1.
            return currentId // Gibt die erwartete ID zurück.
        }
    }
}