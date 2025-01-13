package model

import utils.Priority
import java.time.LocalDateTime
import java.time.LocalTime

data class Task (
    val id: Int, // Unique identifier
    val userId:Int,
    val title: String,                  // Title of the task
    val priority: String,               // Priority (High, Medium, Low)
    val createdAt: LocalDateTime,       // Creation date and time
    var updatedAt: LocalDateTime,       // Last updated date and time
    val startTime: LocalDateTime?,  // NEM LocalTime
    val endTime: LocalDateTime?,    // NEM LocalTime    // End Time
    val deadline: LocalDateTime?,        // Due date
    var status: String,
    val imageBase64: String,
) {

    companion object {
        var currentId: Int = 0

        /**
         * Generates the next unique ID.
         * @return The next ID as an integer.
         */
        fun generateId(): Int {
            currentId++
            return currentId
        }
    }
}