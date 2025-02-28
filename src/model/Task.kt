package model

import java.time.LocalDateTime

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