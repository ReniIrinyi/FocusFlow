package controller

import model.Task
import utils.Constants
import utils.ErrorManager
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

    /**
     * Lädt alle Tasks aus der Datei.
     *
     * @return Ein Paar bestehend aus einer Liste von Tasks und dem HTTP-Statuscode.
     *          - Erfolgsstatus: 200 (Constants.RESTAPI_OK)
     *          - Fehlerstatus: 500 (Constants.RESTAPI_INTERNAL_SERVER_ERROR)
     */
    override fun loadEntities(): Pair<List<Task>, Int> {
        return try {
            // Überprüft, ob die Datei existiert oder leer ist.
            // Falls leer, gibt es eine leere Liste zurück.
            if (!file.exists() || file.readText().isEmpty()) {
                return Pair(emptyList(), ErrorManager.RESTAPI_OK)
            }
            // Liest jede Zeile und konvertiert sie in Task-Objekte.
            val tasks = file.readLines().map { line -> parseTask(line) }
            Pair(tasks, ErrorManager.RESTAPI_OK)
        } catch (e: Exception) {
            // Fehlermeldung, falls beim Laden ein Fehler auftritt.
            println("Fehler beim Laden der Tasks: ${e.message}")
            Pair(emptyList(), ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR)
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
            val file = File(filePath)
            // Erstellt die Datei und ihre Elternverzeichnisse, falls sie nicht existiert.
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }

            // Serialisiert die Tasks und speichert sie zeilenweise in der Datei.
            val lines = entities.map { serializeTask(it) }
            file.writeText(lines.joinToString("\n"))
            ErrorManager.RESTAPI_OK
        } catch (e: Exception) {
            println("Fehler beim Speichern der Tasks: ${e.message}")
            ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR
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
            if (status != ErrorManager.RESTAPI_OK) return ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR

            // Sucht den Index des Tasks in der geladenen Liste basierend auf der ID.
            val taskIndex = tasks.indexOfFirst { it.id == id }
            if (taskIndex == -1) return ErrorManager.RESTAPI_NOT_FOUND

            // Aktualisiert den Task in der Liste und speichert die aktualisierte Liste.
            val updatedTasks = tasks.toMutableList()
            updatedTasks[taskIndex] = updatedData
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            println("Fehler beim Aktualisieren eines Tasks: ${e.message}")
            ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR
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
            if (status != ErrorManager.RESTAPI_OK) return ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR

            // Fügt die neue Task zur Liste hinzu und speichert sie.
            val updatedTasks = tasks.toMutableList()
            updatedTasks.add(newEntity)
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            println("Fehler beim Hinzufügen eines Tasks: ${e.message}")
            ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR
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
            if (status != ErrorManager.RESTAPI_OK) return ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR

            // Filtert den Task mit der angegebenen ID aus der Liste heraus.
            val updatedTasks = tasks.filter { it.id != id }
            if (updatedTasks.size == tasks.size) {
                return ErrorManager.RESTAPI_NOT_FOUND
            }
            saveEntities(updatedTasks)
        } catch (e: Exception) {
            println("Fehler beim Löschen eines Tasks: ${e.message}")
            ErrorManager.RESTAPI_INTERNAL_SERVER_ERROR
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
            escapeField(task.title), // Titel wird zeichenkodiert.
            task.priority,
            task.createdAt.format(formatter),
            task.updatedAt.format(formatter),
            task.deadline?.format(formatter) ?: "", // Falls keine Deadline vorhanden ist, leer lassen.
            task.status,
            task.startTime?.format(formatter),
            task.endTime?.format(formatter),
            task.imageBase64,
            task.userId
        ).joinToString("|") // Trennt die Felder durch „|“.
    }

    /**
     * Parst eine Zeile aus der Datei in ein Task-Objekt.
     */
    private fun parseTask(line: String): Task {
        val tokens = line.split("|")
        return Task(
            id = tokens[0].toInt(),
            title = unescapeField(tokens[1]), // Entfernt die Zeichenkodierung.
            priority = tokens[2],
            createdAt = LocalDateTime.parse(tokens[3]),
            updatedAt = LocalDateTime.parse(tokens[4]),
            deadline = tokens[5].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            status = tokens[6],
            startTime = tokens[7].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            endTime = tokens[8].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            imageBase64 = tokens[9],
            userId = tokens[10].toInt()
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