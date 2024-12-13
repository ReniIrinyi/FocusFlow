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
 * Diese Klasse verwaltet das Speichern, Laden und löschen von Aufgaben aus einer Datei.
 *
 * Ziel:
 * - Persistente Speicherung der Aufgaben, auch nach Beenden der Anwendung.
 * - Verwendung eines benutzerdefinierten Textformats für die Speicherung.
 * - Unabhängigkeit von der Geschäftslogik (TaskService).
 *
 * Funktionen:
 * - `loadTasks()`: Liest die Aufgaben aus der Datei und gibt sie als Liste zurück.
 * - `saveTasks(tasks)`: Speichert die übergebene Liste von Aufgaben in die Datei.
 *
 * Hinweise:
 * - Wenn die Datei nicht existiert, wird sie automatisch erstellt.
 * - Falls die Datei leer ist, wird eine leere Liste zurückgegeben.
 */
open class FileHandler {

    private val filePath = Constants.TASKS_FILE_NAME // Der Speicherort der Aufgaben-Datei.

    /**
     * Lädt alle Aufgaben aus der Datei.
     * @return Eine Liste der gespeicherten Aufgaben (`List<Task>`) oder eine leere Liste bei Fehlern.
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
                    "1" -> "Erledigt"
                    "0" -> "In Bearbeitung"
                    "" -> "Nicht erledigt" // null wird als Nicht erledigt interpretiert
                    else -> "In Bearbeitung" // Fallback
                }

                val priority = when(tokens[2]){
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

    fun updateTask(taskId:Int): Any {
        return try {
            val (tasks, status) = loadTasks();
            val taskToUpdate = tasks.find {it.id===taskId};
            if(taskToUpdate != null){
                return Constants.RESTAPI_NOT_FOUND;
            } else {

            }

        } catch (e:Exception){

        }
    }



    /**
     * Löscht eine Aufgabe anhand der ID.
     * @param taskId Die ID der zu löschenden Aufgabe.
     * @return 200 bei Erfolg, 404 wenn keine Aufgabe mit der ID gefunden wurde, 400 bei anderen Fehlern.
     */
    fun deleteTaskById(taskId: Int): Int {
        return try {
            val (tasks, status) = loadTasks()
            if (status != RESTAPI_OK) {
                return RESTAPI_INTERNAL_SERVER_ERROR
            }

            val taskToDelete = tasks.find { it.id == taskId }
            if (taskToDelete != null) {
                val updatedTasks = tasks.filter { it.id != taskId }
                val saveStatus = saveTasks(updatedTasks)
                if (saveStatus == RESTAPI_OK) {
                    println("Aufgabe mit ID $taskId wurde erfolgreich gelöscht.")
                    RESTAPI_OK
                } else {
                    RESTAPI_INTERNAL_SERVER_ERROR
                }
            } else {
                println("Fehler: Keine Aufgabe mit ID $taskId gefunden.")
                RESTAPI_NOT_FOUND
            }
        } catch (e: Exception) {
            println("Fehler beim Löschen der Aufgabe: ${e.message}")
            RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    /**
     * Aktualisiert die Priorität einer Aufgabe basierend auf ihrer ID.
     *
     * Schritte:
     * 1. Validiert den neuen Prioritätswert.
     * 2. Lädt die Aufgaben aus der Datei.
     * 3. Sucht die Aufgabe mit der gegebenen ID.
     *    - Wenn die Aufgabe gefunden wird: Aktualisiert die Priorität.
     *    - Wenn die Aufgabe nicht gefunden wird: Gibt einen 404-Fehlercode zurück.
     * 4. Speichert die aktualisierte Liste in die Datei.
     *
     * @param taskId Die ID der Aufgabe, die aktualisiert werden soll.
     * @param newPriority Die neue Priorität für die Aufgabe.
     * @return 200 bei Erfolg, 404 wenn keine Aufgabe gefunden wurde, 400 bei ungültiger Priorität oder Fehler.
     */
    fun updateTaskPriority(taskId: Int, newPriority: Enum<Priority>): Int {
        if (newPriority !in listOf(Priority.PRIORITY_HIGH, Priority.PRIORITY_MEDIUM, Priority.PRIORITY_LOW)) {
            println("Fehler: Ungültiger Prioritätswert '$newPriority'. Erlaubte Werte: ${Priority.PRIORITY_HIGH}, ${Priority.PRIORITY_MEDIUM}, ${Priority.PRIORITY_LOW}.")
            return RESTAPI_INTERNAL_SERVER_ERROR
        }

        return try {
            val (tasks, status) = loadTasks()
            if (status != RESTAPI_OK) {
                return RESTAPI_INTERNAL_SERVER_ERROR
            }

            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            if (taskIndex != -1) {
                val task = tasks[taskIndex]
                val updatedTask = task.copy(priority = newPriority.toString())
                val updatedTasks = tasks.toMutableList()
                updatedTasks[taskIndex] = updatedTask

                val saveStatus = saveTasks(updatedTasks)
                if (saveStatus == RESTAPI_OK) {
                    println("Priorität der Aufgabe mit ID $taskId wurde erfolgreich auf $newPriority aktualisiert.")
                    RESTAPI_OK
                } else {
                    RESTAPI_INTERNAL_SERVER_ERROR
                }
            } else {
                println("Fehler: Keine Aufgabe mit ID $taskId gefunden.")
                RESTAPI_NOT_FOUND
            }
        } catch (e: Exception) {
            println("Fehler beim Aktualisieren der Priorität: ${e.message}")
            RESTAPI_INTERNAL_SERVER_ERROR
        }
    }

    fun updateTaskStatus(taskId: Int, newStatus: Int?): Int {
        // Validierung des neuen Statuswerts
        if (newStatus !in listOf(Constants.STATUS_DONE, Constants.STATUS_NOT_DONE, Constants.STATUS_IN_PROGRESS)) {
            println("Fehler: Ungültiger Statuswert '$newStatus'. Erlaubte Werte: ${Constants.STATUS_DONE}, ${Constants.STATUS_NOT_DONE}, ${Constants.STATUS_IN_PROGRESS}.")
            return Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }

        return try {
            val (tasks, status) = loadTasks()
            if (status != Constants.RESTAPI_OK) {
                return Constants.RESTAPI_INTERNAL_SERVER_ERROR
            }

            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            if (taskIndex != -1) {
                val task = tasks[taskIndex]
                val updatedTask = task.copy(status = when (newStatus) {
                    Constants.STATUS_DONE -> "Erledigt"
                    Constants.STATUS_NOT_DONE -> "Nicht erledigt"
                    else -> "In Bearbeitung"
                })
                val updatedTasks = tasks.toMutableList()
                updatedTasks[taskIndex] = updatedTask

                val saveStatus = saveTasks(updatedTasks)
                if (saveStatus == Constants.RESTAPI_OK) {
                    println("Status der Aufgabe mit ID $taskId wurde erfolgreich aktualisiert.")
                    Constants.RESTAPI_OK
                } else {
                    Constants.RESTAPI_INTERNAL_SERVER_ERROR
                }
            } else {
                println("Fehler: Keine Aufgabe mit ID $taskId gefunden.")
                Constants.RESTAPI_NOT_FOUND
            }
        } catch (e: Exception) {
            println("Fehler beim Aktualisieren des Status: ${e.message}")
            Constants.RESTAPI_INTERNAL_SERVER_ERROR
        }
    }



    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }

}
