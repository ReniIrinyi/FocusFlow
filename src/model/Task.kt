package model

import java.time.LocalDateTime

data class Task (
    val id: Int,                        // Unique identifier
    val title: String,                  // Title of the task
    val priority: String,               // Priority (High, Medium, Low)
    val createdAt: LocalDateTime,       // Creation date and time
    var updatedAt: LocalDateTime,       // Last updated date and time
    val deadline: LocalDateTime,        // Due date
    var status: String                  // Status (Created, In Progress, Completed)
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