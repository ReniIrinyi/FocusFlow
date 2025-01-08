package model

import utils.Role

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val role: Role
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