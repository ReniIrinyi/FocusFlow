package controller

import model.User
import utils.Constants
import utils.Role
import java.io.File
import java.security.MessageDigest

/**
 * Die `UserStorage`-Klasse ist spezialisiert auf die Verwaltung von `User`-Entitäten.
 * Sie implementiert das generische Interface `StorageInterface<User>` und bietet
 * Funktionen zum Laden, Speichern, Aktualisieren und Löschen von Benutzern.
 */
class UserStorage : StorageInterface<User> {

    // Dateipfad, in dem die Benutzerdaten gespeichert werden
    private val filePath = Constants.USER_FILE_PATH
    private val file = File(filePath)

    /**
     * Initialisiert die `UserStorage`-Klasse:
     * - Überprüft, ob die Datei existiert, und erstellt sie bei Bedarf.
     * - Initialisiert die aktuelle ID (`User.currentId`) basierend auf den gespeicherten Benutzern.
     */
    init {
        checkIfFilePathExists()
        initializeCurrentId()
    }

    /**
     * Überprüft, ob die Datei zum Speichern der Benutzer existiert.
     * Wenn nicht, wird eine neue Datei erstellt.
     */
    override  fun checkIfFilePathExists() {
        if (!file.exists()) {
            println("Datei $file existiert nicht. Eine neue Datei wird erstellt...")
            file.createNewFile()
        } else {
            println("Datei $file existiert bereits.")
        }
    }

    /**
     * Initialisiert die aktuelle ID (`User.currentId`) basierend auf der höchsten ID
     * der in der Datei gespeicherten Benutzer.
     */
    private fun initializeCurrentId() {
        val users = loadEntities().first
        if (users.isNotEmpty()) {
            User.currentId = users.maxOf { it.id }
        }
    }

    /**
     * Lädt alle Benutzer aus der Datei.
     *
     * @return Ein Pair, das aus:
     *         - Einer Liste von Benutzern (`List<User>`) besteht.
     *         - Einem HTTP-ähnlichen Statuscode (`Int`) besteht: 200 (erfolgreich) oder 500 (Fehler).
     */
    override fun loadEntities(): Pair<List<User>, Int> {
        return try {
            val users = file.readLines()
                .mapNotNull { line -> parseUser(line) } // Konvertiert jede Zeile in ein User-Objekt, falls möglich
            Pair(users, 200)
        } catch (e: Exception) {
            println("Fehler beim Laden der Benutzer: ${e.message}")
            Pair(emptyList(), 500)
        }
    }

    /**
     * Speichert alle Benutzer in der Datei.
     *
     * @param entities Die Liste der Benutzer, die gespeichert werden soll.
     * @return Statuscode: 200 (erfolgreich) oder 500 (Fehler).
     */
    override fun saveEntities(entities: List<User>): Int {
        return try {
            // Serialisiert jeden Benutzer und speichert ihn in der Datei
            file.writeText(entities.joinToString("\n") { serializeUser(it) })
            200
        } catch (e: Exception) {
            println("Fehler beim Speichern der Benutzer: ${e.message}")
            500
        }
    }

    /**
     * Aktualisiert einen Benutzer basierend auf der ID.
     *
     * @param id Die ID des zu aktualisierenden Benutzers.
     * @param updatedData Die neuen Benutzerdaten.
     * @return Statuscode: 200 (erfolgreich), 404 (nicht gefunden) oder 500 (Fehler).
     */
    override fun updateEntity(id: Int, updatedData: User): Int {
        return try {
            val (users, status) = loadEntities()
            if (status != 200) return 500

            val userIndex = users.indexOfFirst { it.id == id } // Sucht den Benutzer anhand der ID
            if (userIndex == -1) return 404

            val updatedUsers = users.toMutableList()
            updatedUsers[userIndex] = updatedData
            saveEntities(updatedUsers) // Speichert die aktualisierte Benutzerliste
        } catch (e: Exception) {
            println("Fehler beim Aktualisieren des Benutzers: ${e.message}")
            500
        }
    }

    /**
     * Fügt einen neuen Benutzer hinzu.
     *
     * @param newEntity Das neue Benutzerobjekt.
     * @return Statuscode: 200 (erfolgreich), 500 (Fehler) oder Exception, wenn ein zweiter Admin hinzugefügt wird.
     */
    override fun addEntity(newEntity: User): Int {
        return try {
            val (users, status) = loadEntities()
            if (status != 200) return 500

            // Verhindert das Hinzufügen eines zweiten Admins
            if (newEntity.role == Role.ADMIN && users.any { it.role == Role.ADMIN }) {
                throw IllegalArgumentException("Es darf nur einen Admin geben!")
            }

            val newUsers = users.toMutableList()
            newUsers.add(newEntity)
            saveEntities(newUsers)
        } catch (e: Exception) {
            println("Fehler beim Hinzufügen des Benutzers: ${e.message}")
            500
        }
    }

    /**
     * Löscht einen Benutzer basierend auf seiner ID.
     *
     * @param id Die ID des Benutzers, der gelöscht werden soll.
     * @return Statuscode: 200 (erfolgreich), 404 (nicht gefunden) oder 500 (Fehler).
     */
    override fun deleteEntityById(id: Int): Int {
        return try {
            val (users, status) = loadEntities()
            if (status != 200) return 500

            val updatedUsers = users.filter { it.id != id } // Filtert den Benutzer mit der angegebenen ID heraus
            if (updatedUsers.size == users.size) return 404 // Keine Änderungen, Benutzer nicht gefunden

            saveEntities(updatedUsers)
        } catch (e: Exception) {
            println("Fehler beim Löschen des Benutzers: ${e.message}")
            500
        }
    }

    // Hilfsmethoden zur Konvertierung zwischen Benutzer und Datei

    /**
     * Konvertiert ein `User`-Objekt in ein textbasiertes Format, um es in die Datei zu speichern.
     */
    private fun serializeUser(user: User): String {
        return listOf(user.id, user.name, user.email, user.password, user.role).joinToString("|")
    }

    /**
     * Parst eine Textzeile aus der Datei und konvertiert sie zurück in ein `User`-Objekt.
     */
    private fun parseUser(line: String): User? {
        val parts = line.split("|")
        return if (parts.size == 5) {
            User(
                id = parts[0].toInt(),
                name = parts[1],
                email = parts[2],
                password = parts[3],
                role = Role.valueOf(parts[4])
            )
        } else {
            null
        }
    }
}