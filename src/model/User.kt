package model

import utils.Role

/**
 * Die `User`-Datenklasse repräsentiert Benutzerinformationen innerhalb des Systems.
 *
 * @property id Die eindeutige ID des Benutzers. Diese wird automatisch generiert.
 * @property name Der vollständige Name des Benutzers.
 * @property email Die E-Mail-Adresse des Benutzers, um ihn zu kontaktieren oder zur Authentifizierung zu nutzen.
 * @property password Das Passwort des Benutzers (sollte gehashed gespeichert werden).
 * @property role Die Rolle des Benutzers im System, z. B. ADMIN oder USER.
 */
data class User (
    val id: Int,                      // Die eindeutige ID des Benutzers
    val name: String,                 // Vollständiger Name des Benutzers
    val email: String,                // E-Mail-Adresse des Benutzers
    val password: String,             // Passwort des Benutzers (idealerweise gehashed)
    val role: Role                    // Rolle des Benutzers (z. B. ADMIN oder USER)
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
            return currentId // Gibt die nächste eindeutige ID zurück.
        }
    }
}