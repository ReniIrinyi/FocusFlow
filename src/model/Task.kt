package model

import java.time.LocalDateTime

/**
 * Die `Task`-Datenklasse repräsentiert eine Aufgabe innerhalb des Systems.
 * Jede Aufgabe gehört zu einem Benutzer und enthält alle Details wie Titel, Priorität, Zeiten und Status.
 *
 * @property id Eindeutiger Bezeichner der Aufgabe.
 * @property userId Die ID des Benutzers, dem die Aufgabe zugewiesen ist.
 * @property title Der Titel der Aufgabe.
 * @property priority Die Priorität der Aufgabe (Hoch = 1, Mittel = 2, Niedrig = 3).
 * @property createdAt Das Datum und die Uhrzeit der Erstellung der Aufgabe.
 * @property updatedAt Das Datum und die Uhrzeit der letzten Aktualisierung der Aufgabe.
 * @property startTime Startzeitpunkt der Aufgabe.
 * @property endTime Abschluss der Aufgabe.
 * @property deadline Fälligkeitsdatum der Aufgabe (optional).
 * @property status Der aktuelle Status der Aufgabe (Nicht erledigt = 0, In Bearbeitung =1, Erledigt = 2).
 * @property imageBase64 Ein Bild im Base64-String-Format, das mit der Aufgabe verknüpft ist (z. B. ein Screenshot).
 * @property description Beschreibung der Aufgabe.
 */
data class Task (
    val id: Int,
    val userId: Int,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    val priority: Int,
    var status: Int,
    val title: String,
    val description:String,
    val startTime: LocalDateTime,
    val deadline: LocalDateTime?,
    val endTime: LocalDateTime,
    val imageBase64: String,
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
            return currentId
        }
    }
}