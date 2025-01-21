package service

import model.User
import controller.UserStorage
import utils.Constants
import java.security.MessageDigest

/**
 * Die UserService-Klasse dient der Verwaltung von User-Entitäten (Benutzern).
 * Diese Klasse implementiert das CrudService-Interface für den Typ "User".
 */
class UserService : CrudService<User> {

    // Verbindung zur UserStorage-Klasse, die die Speicherung der Entitäten verwaltet.
    private val storage = UserStorage()

    /**
     * Ruft alle User-Entitäten aus dem Speicher ab.
     *
     * @return Eine Liste aller gespeicherten Benutzer.
     */
    override fun findAll(): List<User> {
        return storage.loadEntities().first // Lädt alle Benutzer (Users) und gibt die Liste zurück.
    }

    /**
     * Ruft einen spezifischen User anhand seiner eindeutigen ID ab.
     *
     * @param id Die ID des gesuchten Benutzers.
     * @return Der gefundene Benutzer, falls vorhanden, ansonsten null.
     */

    override fun findById(id: Int): User? {
        return findAll().find { it.id == id } // Sucht nach einem Benutzer mit der angegebenen ID in der Liste.
    }

    fun createAdmin(username: String, email: String, password: String, img: String ="") {
        // Generiere eine eindeutige Benutzer-ID.
        val userId = User.generateId()

        // Erstelle den neuen Admin-Benutzer.
        val adminUser = User(
            id = userId,
            name = username,
            email = email,
            password = hashPassword(password), // Sichere Speicherung des Passworts.
            role = Constants.ROLE_ADMIN,
            profileImage = img
        )

        // Speichere den neuen Benutzer mit dem existierenden `save`-Mechanismus.
        save(adminUser)
    }

    /**
     * Speichert einen neuen User oder aktualisiert einen bestehenden.
     *
     * @param entity Der Benutzer, der gespeichert oder aktualisiert werden soll.
     */
    override fun save(entity: User) {
        val users = findAll() // Ruft alle Benutzer ab.
        val existingUser = users.find { it.id == entity.id } // Überprüft, ob der Benutzer bereits existiert.

        if (existingUser == null) {
            // Wenn der Benutzer nicht existiert, wird er als neuer hinzugefügt.
            storage.addEntity(entity)
        } else {
            // Falls der Benutzer existiert, wird er aktualisiert.
            storage.updateEntity(entity.id, entity)
        }
    }

    /**
     * Löscht eine User-Entität anhand ihrer ID.
     *
     * @param id Die ID des zu löschenden Benutzers.
     */
    override fun delete(id: Int) {
        storage.deleteEntityById(id) // Löscht den Benutzer anhand der ID.
    }


    fun isAdminExists(): Boolean {
        println(storage.loadEntities())
        println(storage.loadEntities().first.find { it.role ==  Constants.ROLE_ADMIN })
        println(storage.loadEntities().first.any { it.role ==  Constants.ROLE_ADMIN })
        return storage.loadEntities().first.any { it.role ==  Constants.ROLE_ADMIN }
    }

    /**
     * Gibt den ersten existierenden Admin-Benutzer (falls vorhanden) zurück.
     *
     * @return Der Admin-Benutzer oder `null`, falls kein Admin gefunden wurde.
     */
    fun getAdmin(): User? {
        return storage.loadEntities().first.find { it.role ==  Constants.ROLE_ADMIN }
    }

    fun updateAdminPassword(newPassword: String) {
        val users = findAll()

        // Sucht den Benutzer mit dem Benutzernamen
        val user = users.find { it.role == Constants.ROLE_ADMIN }
            ?: throw IllegalArgumentException("Admin mit dem angegebenen Benutzernamen existiert nicht!")

        // Neues Passwort hashen
        val hashedPassword = hashPassword(newPassword)

        // Aktualisiertes User-Objekt erstellen
        val updatedUser = user.copy(password = hashedPassword)

        // Benutzer im Speicher aktualisieren
        storage.updateEntity(user.id, updatedUser)
    }

    fun validateUser(username: String, inputPassword: String?): Boolean {
        val users = storage.loadEntities().first // Lädt alle Benutzer aus der Datenquelle.

        // Sucht den Benutzer mit dem Benutzernamen.
        val user = users.find { it.name == username } ?: return false // Benutzer existiert nicht.

        return when (user.role) {
            Constants.ROLE_ADMIN -> {
                // Admins erfordern eine Passwortprüfung.
                val hashedInputPassword = hashPassword(inputPassword ?: "")
                user.password == hashedInputPassword
            }
            else -> {
                // Reguläre Benutzer erfordern keine Passwortprüfung.
                true
            }
        }
    }

    /**
     * Hash-Funktion für Passwörter. Verwendet den SHA-256-Algorithmus, um das Passwort
     * zu verschlüsseln, bevor es gespeichert wird.
     *
     * @param password Das Klartext-Passwort.
     * @return Der Hashwert des Passworts als hexadezimale Zeichenfolge.
     */
    fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }





}