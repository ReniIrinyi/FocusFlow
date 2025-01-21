package model

/**
 * Die `User`-Datenklasse repräsentiert Benutzerinformationen innerhalb des Systems.
 *
 * @property id Die eindeutige ID des Benutzers. Diese wird automatisch generiert.
 * @property name Der vollständige Name des Benutzers.
 * @property email Die E-Mail-Adresse des Benutzers, um ihn zu kontaktieren oder zur Authentifizierung zu nutzen.
 * @property password Das Passwort des Benutzers (gehashed).
 * @property role Die Rolle des Benutzers im System (Admin(Super-User) = 1, User = 2)
 */
data class User (
    val id: Int,
    var name: String,
    val email: String,
    val password: String,
    val role: Int,
    var profileImage:String
) {
    companion object {
        var currentId: Int = 0 // Hält die aktuelle ID, die für den nächsten Benutzer verwendet wird.

        /**
         * Generiert eine neue, eindeutige ID für den Benutzer.
         *
         * @return Die nächste ID als Ganzzahl.
         */
        fun generateId(): Int {
            currentId++ // Erhöht die aktuelle ID um 1.
            return currentId
        }

        fun roleToString(role:Int): String {
            return when (role) {
                1 -> "Admin"
                2 -> "User"
                else -> "Unknown"
            }
        }

        fun stringToRole(role: String): Int {
            return when (role.lowercase()) {
                "admin" -> 1
                "user" -> 2
                else -> throw IllegalArgumentException("Unknown role: $role")
            }
        }


    }


}