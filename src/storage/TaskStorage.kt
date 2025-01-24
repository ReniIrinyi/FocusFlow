package storage

import model.Task
import utils.Constants
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class TaskStorage : StorageInterface<Task> {

    // Der Pfad, wo die Task-Elemente gespeichert werden.
    private val filePath = Constants.TASKS_FILE_PATH
    private val file = File(filePath)


    init {
        checkIfFilePathExists()
    }

    /**
     * Behandelt verschiedene HTTP-Anfragen für Aufgaben (Tasks).
     *
     * Unterstützte Anfragetypen:
     * - **GET**: Ruft Aufgaben basierend auf dem angegebenen Pfad ab.
     *      - "all"       -> Gibt alle Aufgaben zurück.
     *      - "byId"      -> Gibt eine bestimmte Aufgabe anhand der übergebenen ID zurück.
     *      - "byUserId"  -> Gibt alle Aufgaben für einen bestimmten Benutzer zurück.
     *
     * - **PUT**: Aktualisiert eine bestehende Aufgabe mit den bereitgestellten Daten.
     *      - Erforderlich: ID der Aufgabe und neue Daten (`newData`).
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * - **DELETE**: Löscht eine Aufgabe anhand der übergebenen ID.
     *      - Erforderlich: ID der Aufgabe.
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * - **POST**: Erstellt eine neue Aufgabe mit den bereitgestellten Daten.
     *      - Erforderlich: Neue Daten (`newData`).
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * @param routePath Der spezifische Pfad der Anfrage (z.B. "all", "byId", "byUserId").
     * @param requestTyp Der Typ der HTTP-Anfrage (GET, PUT, DELETE, POST).
     * @param Id Die eindeutige ID der Aufgabe (nur für "byId" oder DELETE-Anfragen erforderlich).
     * @param userId Die Benutzer-ID für benutzerbezogene Anfragen (nur für "byUserId" erforderlich).
     * @param newData Die neuen Daten für PUT- oder POST-Anfragen (optional).
     * @return Ein Paar bestehend aus der Antwort (Ergebnis oder Fehlermeldung) und dem HTTP-Statuscode.
     */
    override fun getRequest(requestTyp: String, Id: Int?, userId: Int?, newData: Task?,routePath: String?): Pair<Any, Int> {
        return when (requestTyp) {
            Constants.GET -> {
                val (tasks, status) = this.loadEntities()

                if (status != Constants.RESTAPI_OK) {
                    return Pair("Fehler beim Laden der Tasks", Constants.RESTAPI_INTERNAL_SERVER_ERROR)
                }

                when (routePath) {
                    "all" -> Pair(tasks, Constants.RESTAPI_OK)

                    "byId" -> {
                        val task = tasks.find { it.id == Id }
                        if (task != null) {
                            Pair(task, Constants.RESTAPI_OK)
                        } else {
                            Pair("Task nicht gefunden", Constants.RESTAPI_NOT_FOUND)
                        }
                    }

                    "byUserId" -> {
                        val userTasks = tasks.filter { it.userId == userId }
                        if (userTasks.isNotEmpty()) {
                            Pair(userTasks, Constants.RESTAPI_OK)
                        } else {
                            Pair(emptyList<Task>(), Constants.RESTAPI_NOT_FOUND)
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
                    Pair("Kein Id angegeben", Constants.RESTAPI_BAD_REQUEST)
                }

            }

            Constants.POST -> {
                if (newData != null) {
                    this.addEntity(newData)
                    Pair("Neuer Task erfolgreich hinzugefügt.", Constants.RESTAPI_OK)
                } else {
                    Pair("Fehler: Keine Daten zum Hinzufügen.", Constants.RESTAPI_BAD_REQUEST)
                }
            }

            else -> Pair("Ungültiger Anfrage-Typ.", Constants.RESTAPI_BAD_REQUEST)
        }
    }


    /**
     * Überprüft, ob die Datei zum Speichern der Aufgaben existiert.
     * Wenn nicht, wird eine neue Datei erstellt.
     */
    override fun checkIfFilePathExists() {
        if (!file.exists()) {
            println("Datei $file existiert nicht. Eine neue Datei wird erstellt...")
            file.createNewFile()
        }
    }

    /**
     * Lädt alle Tasks aus der Datei.
     *
     * @return Ein Paar bestehend aus einer Liste von Tasks und dem HTTP-Statuscode.
     *          - Erfolgsstatus: 200 (Constants.RESTAPI_OK)
     *          - Fehlerstatus: 500 (Constants.RESTAPI_INTERNAL_SERVER_ERROR)
     */
    override fun loadEntities(): Pair<List<Task>, Int> {
        return try {
            // Überprüft, ob die Datei leer ist.
            // Falls leer, gibt es eine leere Liste zurück.
            if (file.readText().isEmpty()) {
                return Pair(emptyList(), Constants.RESTAPI_OK)
            }
            // Liest jede Zeile und konvertiert sie in Task-Objekte.
            val tasks = file.readLines().map { line -> parseTask(line) }
            Pair(tasks, Constants.RESTAPI_OK)
        } catch (e: Exception) {
            Pair(emptyList(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Speichert eine Liste von Tasks in der Datei.
     *
     * @param entities Die Liste von Tasks, die gespeichert werden sollen.
     * @return Erfolgsstatus (200) oder Fehlerstatus (500).
     */
    override fun saveEntities(entities: List<Task>): Int {
        return try {
            // Serialisiert die Tasks und speichert sie zeilenweise in der Datei.
            val lines = entities.map { serializeTask(it) }
            file.writeText(lines.joinToString("\n"))
            Constants.RESTAPI_OK
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Aktualisiert einen vorhandenen Task basierend auf der ID.
     *
     * @param id Die ID des zu aktualisierenden Tasks.
     * @param updatedData Die neuen Daten des Tasks.
     * @return HTTP-Statuscode: 200 (erfolgreich), 404 (nicht gefunden) oder 500 (Fehler).
     */
    override fun updateEntity(id: Int, updatedData: Task): Int {
        return try {
            val (tasks, status) = loadEntities()
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR

            // Sucht den Index des Tasks in der geladenen Liste basierend auf der ID.
            val taskIndex = tasks.indexOfFirst { it.id == id }
            if (taskIndex == -1) return Constants.RESTAPI_NOT_FOUND

            // Aktualisiert den Task in der Liste und speichert die aktualisierte Liste.
            val updatedTasks = tasks.toMutableList()
            updatedTasks[taskIndex] = updatedData
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Fügt einen neuen Task zur Datei hinzu.
     *
     * @param newEntity Der neue Task, der hinzugefügt werden soll.
     * @return HTTP-Statuscode: 200 (erfolgreich) oder 500 (Fehler).
     */
    override fun addEntity(newEntity: Task): Int {
        return try {
            val (tasks, status) = loadEntities()
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR
            // Fügt die neue Task zur Liste hinzu und speichert sie.
            val updatedTasks = tasks.toMutableList()
            updatedTasks.add(newEntity)
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Löscht einen Task basierend auf seiner ID.
     *
     * @param id Die ID des zu löschenden Tasks.
     * @return HTTP-Statuscode: 200 (erfolgreich gelöscht),
     *          404 (Task nicht gefunden) oder 500 (Fehler).
     */
    override fun deleteEntityById(id: Int): Int {
        return try {
            val (tasks, status) = loadEntities()
            if (status != Constants.RESTAPI_OK) return Constants.RESTAPI_INTERNAL_SERVER_ERROR

            // Filtert den Task mit der angegebenen ID aus der Liste heraus.
            val updatedTasks = tasks.filter { it.id != id }
            if (updatedTasks.size == tasks.size) {
                return Constants.RESTAPI_NOT_FOUND
            }
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    // Hilfsfunktionen zur Serialisierung und Deserialisierung von Task-Objekten.

    /**
     * Serialisiert ein Task-Objekt in ein String-Format für die Speicherung in der Datei.
     */
    private fun serializeTask(task: Task): String {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return listOf(
            task.id,
            task.userId,
            task.createdAt.format(formatter),
            task.updatedAt.format(formatter),
            task.priority,
            task.status,
            escapeField(task.title),
            escapeField(task.description),
            task.startTime.format(formatter),
            task.deadline?.format(formatter) ?: "",
            task.endTime.format(formatter),
            task.imageBase64
        ).joinToString("|")
    }

    /**
     * Parst eine Zeile aus der Datei in ein Task-Objekt.
     */
    private fun parseTask(line: String): Task {
        val tokens = line.split("|")
        return Task(
            id = tokens[0].toInt(),
            userId = tokens[1].toInt(),
            createdAt = LocalDateTime.parse(tokens[2]),
            updatedAt = LocalDateTime.parse(tokens[3]),
            priority = tokens[4].toInt(),
            status = tokens[5].toInt(),
            title = unescapeField(tokens[6]),
            description = unescapeField(tokens[7]),
            startTime = LocalDateTime.parse(tokens[8]),
            deadline = tokens[9].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            endTime = LocalDateTime.parse(tokens[10]),
            imageBase64 = tokens[11]
        )
    }

    /**
     * Kodiert problematische Zeichen im Feld, damit sie sicher gespeichert werden können.
     */
    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    /**
     * Dekodiert die gespeicherten Zeichen zurück in ihr Originalform.
     */
    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }


}