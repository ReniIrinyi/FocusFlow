package storage

import model.User
import utils.Constants
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
     * Behandelt verschiedene HTTP-Anfragen für Benutzer (User).
     *
     * Unterstützte Anfragetypen:
     * - **GET**: Ruft Benutzerdaten basierend auf dem angegebenen Pfad ab.
     *      - "all"            -> Gibt alle Benutzer zurück.
     *      - "byId"           -> Gibt einen bestimmten Benutzer anhand der übergebenen ID zurück.
     *      - "isAdminExists"  -> Prüft, ob ein Administrator vorhanden ist.
     *      - "getAdmin"       -> Gibt den Administrator zurück, falls vorhanden.
     *      - "validateUser"   -> Validiert Benutzeranmeldedaten.
     *      - "updatePasswort" -> Aktualisiert das Administratorpasswort.
     *
     * - **PUT**: Aktualisiert einen bestehenden Benutzer mit den bereitgestellten Daten.
     *      - Erforderlich: ID des Benutzers und neue Daten (`newData`).
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * - **DELETE**: Löscht einen Benutzer anhand der übergebenen ID.
     *      - Erforderlich: ID des Benutzers.
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * - **POST**: Erstellt einen neuen Benutzer mit den bereitgestellten Daten.
     *      - Erforderlich: Neue Daten (`newData`).
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * @param routePath Der spezifische Pfad der Anfrage (z.B. "all", "byId", "isAdminExists", "getAdmin", "validateUser", "updatePasswort").
     * @param requestTyp Der Typ der HTTP-Anfrage (GET, PUT, DELETE, POST).
     * @param Id Die eindeutige ID des Benutzers (nur für "byId" oder DELETE-Anfragen erforderlich).
     * @param userId Benutzer-ID für benutzerbezogene Anfragen (optional).
     * @param newData Die neuen Daten für PUT- oder POST-Anfragen (optional).
     * @return Ein Paar bestehend aus der Antwort (Ergebnis oder Fehlermeldung) und dem HTTP-Statuscode.
     */
    override fun getRequest(requestTyp: String, Id: Int?, userId: Int?, newData: User?,routePath: String?): Pair<Any, Int> {
        return when (requestTyp) {
            Constants.GET -> {
                val (users, status) = this.loadEntities()

                if (status != Constants.RESTAPI_OK) {
                    return Pair("Fehler beim Laden der Benutzer", Constants.RESTAPI_INTERNAL_SERVER_ERROR)
                }

                println(routePath)
                when (routePath) {
                    "all" -> Pair(users, Constants.RESTAPI_OK)

                    "byId" -> {
                        val user = users.find { it.id == userId }
                        if (user != null) {
                            Pair(user, Constants.RESTAPI_OK)
                        } else {
                            Pair("User nicht gefunden", Constants.RESTAPI_NOT_FOUND)
                        }
                    }
                    "isAdminExists" -> {
                            Pair(this.isAdminExists(), Constants.RESTAPI_OK)
                    }
                    "getAdmin" -> {
                        val admin = this.getAdmin()
                        if(admin != null){
                            Pair(admin, Constants.RESTAPI_NOT_FOUND)
                        } else {
                            Pair("Fehler:Benutzer mit Rolle 'Admin' ist nicht hinterlegt", Constants.RESTAPI_NOT_FOUND)
                        }
                    }
                    "validateUser"->{
                        if(newData != null) {
                            val isUserValid = this.validateUser(newData.name, newData.password)
                            Pair(isUserValid,Constants.RESTAPI_OK)
                        } else {
                            Pair(false, Constants.RESTAPI_BAD_REQUEST)
                        }
                    }
                    "updatePasswort"->{
                        if(newData != null){
                            this.updateAdminPassword(newData.password)
                            Pair("Passwort aktualisiert",Constants.RESTAPI_OK)
                        } else {
                            Pair("Kein neue Datensatz gefunden", Constants.RESTAPI_BAD_REQUEST)
                        }
                    }


                    else -> Pair("Ungültiger Pfad", Constants.RESTAPI_BAD_REQUEST)
                }
            }

            Constants.PUT -> {
                if (newData != null && Id != null) {
                    val result = this.updateEntity(Id, newData)
                    Pair("Task erfolgreich aktualisiert.", result)
                } else {
                    Pair("Fehler: Keine Daten zum Aktualisieren angegeben.", Constants.RESTAPI_BAD_REQUEST)
                }
            }

            Constants.DELETE -> {
                if(Id != null) {
                    val result = this.deleteEntityById(Id)
                    if (result == Constants.RESTAPI_OK) {
                        Pair("Task erfolgreich gelöscht.", Constants.RESTAPI_OK)
                    } else {
                        Pair("Task nicht gefunden.", Constants.RESTAPI_NOT_FOUND)
                    }
                } else {
                    Pair("Kein Id hinzugefügt", Constants.RESTAPI_BAD_REQUEST)
                }

            }

            Constants.POST -> {
                if (newData != null) {
                    this.addEntity(newData)
                    Pair("Neuer User erfolgreich hinzugefügt.", Constants.RESTAPI_OK)
                } else {
                    Pair("Fehler: Keine Daten zum Hinzufügen.", Constants.RESTAPI_BAD_REQUEST)
                }
            }

            else -> Pair("Ungültiger Anfrage-Typ.", Constants.RESTAPI_BAD_REQUEST)
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
            Pair(users, Constants.RESTAPI_OK)
        } catch (e: Exception) {
            println("Fehler beim Laden der Benutzer: ${e.message}")
            Pair(emptyList(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
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
            Constants.RESTAPI_OK
        } catch (e: Exception) {
            println("Fehler beim Speichern der Benutzer: ${e.message}")
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
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
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR

            // Sucht den Benutzer anhand der ID
            val userIndex = users.indexOfFirst { it.id == id }
            if (userIndex == -1) return Constants.RESTAPI_NOT_FOUND

            val updatedUsers = users.toMutableList()
            updatedUsers[userIndex] = updatedData
            saveEntities(updatedUsers)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR

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
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR

            // Verhindert das Hinzufügen eines zweiten Admins
            if (newEntity.role ==  Constants.ROLE_ADMIN && users.any { it.role ==  Constants.ROLE_ADMIN }) {
                Constants.RESTAPI_ADMIN_EXISTS
            }

            val newUsers = users.toMutableList()
            newUsers.add(newEntity)
            saveEntities(newUsers)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR

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
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR

            // Filtert den Benutzer mit der angegebenen ID heraus
            val updatedUsers = users.filter { it.id != id }
            if (updatedUsers.size == users.size) {
                return Constants.RESTAPI_NOT_FOUND
            }
            saveEntities(updatedUsers)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR

        }
    }

    // Hilfsmethoden zur Konvertierung zwischen Benutzer und Datei

    /**
     * Konvertiert ein `User`-Objekt in ein textbasiertes Format, um es in die Datei zu speichern.
     */
    private fun serializeUser(user: User): String {
        return listOf(user.id, user.name, user.email, user.password, user.role, user.profileImage).joinToString("|")
    }

    /**
     * Parst eine Textzeile aus der Datei und konvertiert sie zurück in ein `User`-Objekt.
     */
    private fun parseUser(line: String): User? {
        val tokens = line.split("|")
        return if (tokens.size == 6) {
            User(
                id = tokens[0].toInt(),
                name = tokens[1],
                email = tokens[2],
                password = tokens[3],
                role = tokens[4].toInt(),
                profileImage = tokens[5]
            )
        } else {
            null
        }
    }

    fun isAdminExists(): Boolean {
        println("here")
        return loadEntities().first.any { it.role ==  Constants.ROLE_ADMIN }
    }

    /**
     * Gibt den ersten existierenden Admin-Benutzer (falls vorhanden) zurück.
     *
     * @return Der Admin-Benutzer oder `null`, falls kein Admin gefunden wurde.
     */
    fun getAdmin(): User? {
        val admin = loadEntities().first.find { it.role == Constants.ROLE_ADMIN }
        if (admin != null) {
            return admin
        } else return null
    }


    fun validateUser(username: String, inputPassword: String?): Boolean {
        val users = loadEntities().first // Lädt alle Benutzer aus der Datenquelle.

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


    fun updateAdminPassword(newPassword: String) {
        val users = loadEntities().first

        // Sucht den Benutzer mit dem Benutzernamen
        val user = users.find { it.role == Constants.ROLE_ADMIN }
            ?: throw IllegalArgumentException("Admin mit dem angegebenen Benutzernamen existiert nicht!")

        // Neues Passwort hashen
        val hashedPassword = hashPassword(newPassword)

        // Aktualisiertes User-Objekt erstellen
        val updatedUser = user.copy(password = hashedPassword)

        // Benutzer im Speicher aktualisieren
        updateEntity(user.id, updatedUser)
    }
}