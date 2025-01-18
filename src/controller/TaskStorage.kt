package controller

import model.Task
import utils.Constants
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Die Klasse TaskStorage implementiert das StorageInterface für den Task-Typ.
 * Sie ist verantwortlich für das Speichern, Abrufen, Aktualisieren und Löschen von Task-Entitäten
 * sowie für das Arbeiten mit der zugrunde liegenden Datei.
 */
class TaskStorage : StorageInterface<Task> {

    // Der Pfad, wo die Task-Elemente gespeichert werden.
    private val filePath = Constants.TASKS_FILE_PATH
    private val file = File(filePath)


    init {
        checkIfFilePathExists()
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