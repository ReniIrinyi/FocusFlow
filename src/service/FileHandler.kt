package service

import model.Task
import utils.Constants
import utils.Constants.RESTAPI_INTERNAL_SERVER_ERROR
import utils.Constants.RESTAPI_NOT_FOUND
import utils.Constants.RESTAPI_OK
import utils.Priority
import java.io.File
import java.time.LocalDateTime

/**
 * Diese Klasse verwaltet das Speichern, Laden und Löschen von Aufgaben aus einer Datei.
 *
 * Ziel:
 * - Persistente Speicherung der Aufgaben, auch nach Beenden der Anwendung.
 * - Verwendung eines benutzerdefinierten Textformats für die Speicherung.
 * - Unabhängigkeit von der Geschäftslogik (TaskService).
 *
 * Funktionen:
 * - `loadTasks()`: Liest die Aufgaben aus der Datei und gibt sie als Liste zurück.
 * - `saveTasks(tasks)`: Speichert die übergebene Liste von Aufgaben in die Datei.
 * - `updateTask(taskId, updatedData)`: Aktualisiert eine Aufgabe basierend auf der ID.
 * - `addTask(newTask)`: Fügt eine neue Aufgabe hinzu.
 *
 */
open class FileHandler {

    private val filePath = Constants.TASKS_FILE_NAME // Der Speicherort der Aufgaben-Datei.

    /**
     * Lädt alle Aufgaben aus der Datei.
     * @return Eine Liste der gespeicherten Aufgaben (`List<Task>`) oder eine leere Liste bei Fehlern, und der StatusCode.
     */
    fun loadTasks(): Pair<List<Task>, Int> {
        return try {
            val file = File(filePath)

            if (!file.exists() || file.readText().isEmpty()) {
                return Pair(emptyList(), Constants.RESTAPI_OK)
            }

            val tasks = file.readLines().map { line ->
                val tokens = line.split("|")

                val id = tokens[0].toInt()
                val title = unescapeField(tokens[1])
                val createdAt = LocalDateTime.parse(tokens[3])
                val updatedAt = LocalDateTime.parse(tokens[4])
                val deadline = tokens[5].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }

                // Status aus Zahl laden
                val status = when (tokens[6]) {
                    "2" -> "Erledigt"
                    "1" -> "In Bearbeitung"
                    else -> "Nicht erledigt" // Fallback
                }

                val priority = when (tokens[2]) {
                    Priority.PRIORITY_HIGH.toString() -> "Hohe Priorität"
                    Priority.PRIORITY_LOW.toString() -> "Niedrige Priorität"
                    Priority.PRIORITY_MEDIUM.toString() -> "Mittlere Priorität"
                    else -> "Keine Priorität"
                }

                Task(
                    id = id,
                    title = title,
                    priority = priority,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                    deadline = deadline,
                    status = status
                )
            }
            println(tasks.toString())
            Pair(tasks, Constants.RESTAPI_OK)
        } catch (e: Exception) {
            println("Fehler beim Laden der Aufgaben: ${e.message}")
            Pair(emptyList(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
        }
    }

    /**
     * Speichert alle Aufgaben in der Datei.
     * @param tasks Die Liste der Aufgaben, die gespeichert werden sollen.
     * @return 200 bei Erfolg, 400 bei Fehler.
     */
    fun saveTasks(tasks: List<Task>): Int {
        return try {
            val file = File(filePath)

            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }

            val lines = tasks.map { task ->
                // Status als Zahl speichern
                val statusValue = when (task.status) {
                    Constants.STATUS_DONE.toString() -> "1"
                    Constants.STATUS_IN_PROGRESS.toString() -> "0"
                    else -> ""
                }

                "${task.id}|${escapeField(task.title)}|${escapeField(task.priority.toString())}|${task.createdAt}|${task.updatedAt}|${task.deadline ?: ""}|$statusValue"
            }

            file.writeText(lines.joinToString("\n"))
            Constants.RESTAPI_OK
        } catch (e: Exception) {
            println("Fehler beim Speichern der Aufgaben: ${e.message}")
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Aktualisiert eine vorhandene Aufgabe basierend auf ihrer ID.
     * @param taskId Die ID der zu aktualisierenden Aufgabe.
     * @param updatedData Die neuen Daten für die Aufgabe.
     * @return 200 bei Erfolg, 404 wenn keine Aufgabe gefunden wurde, 500 bei Fehler.
     */
    fun updateTask(taskId: Int, updatedData: Task): Int {
        return try {
            val (tasks, status) = loadTasks()
            if (status != RESTAPI_OK) return RESTAPI_INTERNAL_SERVER_ERROR

            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            if (taskIndex != -1) {
                val updatedTasks = tasks.toMutableList()
                updatedTasks[taskIndex] = updatedData
                saveTasks(updatedTasks)
                println("Aufgabe mit ID $taskId wurde erfolgreich aktualisiert.")
                RESTAPI_OK
            } else {
                println("Fehler: Keine Aufgabe mit ID $taskId gefunden.")
                RESTAPI_NOT_FOUND
            }
        } catch (e: Exception) {
            println("Fehler beim Aktualisieren der Aufgabe: ${e.message}")
            RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Fügt eine neue Aufgabe hinzu.
     * @param newTask Die neue Aufgabe, die hinzugefügt werden soll.
     * @return 200 bei Erfolg, 500 bei Fehler.
     */
    fun addTask(newTask: Task): Int {
        return try {
            val (tasks, status) = loadTasks()
            if (status != RESTAPI_OK) return RESTAPI_INTERNAL_SERVER_ERROR

            val updatedTasks = tasks.toMutableList()
            updatedTasks.add(newTask)
            saveTasks(updatedTasks)
            println("Neue Aufgabe mit ID ${newTask.id} wurde erfolgreich hinzugefügt.")
            RESTAPI_OK
        } catch (e: Exception) {
            println("Fehler beim Hinzufügen der Aufgabe: ${e.message}")
            RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }
}
