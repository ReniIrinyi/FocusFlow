package service

import model.Task
import utils.Constants
import utils.Constants.RESTAPI_INTERNAL_SERVER_ERROR
import utils.Constants.RESTAPI_NOT_FOUND
import utils.Constants.RESTAPI_OK
import utils.Priority
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
open class FileHandler:TaskStorageInterface {

    private val filePath = Constants.TASKS_FILE_NAME // Der Speicherort der Aufgaben-Datei.

    /**
     * Lädt alle Aufgaben aus der Datei.
     * @return Eine Liste der gespeicherten Aufgaben (`List<Task>`) oder eine leere Liste bei Fehlern, und der StatusCode.
     */
    override fun loadTasks(): Pair<List<Task>, Int> {
        return try {
            val file = File(filePath)

            if (!file.exists() || file.readText().isEmpty()) {
                return Pair(emptyList(), Constants.RESTAPI_OK)
            }

            val tasks = file.readLines().map { line ->
                val tokens = line.split("|")

                val id = tokens[0].toInt()
                val title = unescapeField(tokens[1])

                // KORÁBBAN:
                // val createdAt = LocalDateTime.parse(tokens[3])
                // val updatedAt = LocalDateTime.parse(tokens[4])
                // => Ha biztosan mindig "yyyy-MM-ddTHH:mm" formátum, maradhat
                // Ha lehet "csak óra:perc", használd a lenti segédfüggvényt.

                val createdAt = parseFlexibleDateTime(tokens[3]) ?: LocalDateTime.now()
                val updatedAt = parseFlexibleDateTime(tokens[4]) ?: LocalDateTime.now()

                val deadline = tokens[5].takeIf { it.isNotEmpty() }
                    ?.let { parseFlexibleDateTime(it) }

                // FIGYELEM: a 7. és 8. indexben startTime, endTime szerepelhet
                // Lehet "17:00" vagy "2023-10-04T17:00" is.
                val startTimeStr = tokens.getOrNull(7)?.trim().orEmpty()
                val endTimeStr   = tokens.getOrNull(8)?.trim().orEmpty()

                val startTime = if (startTimeStr.isNotEmpty()) {
                    parseFlexibleDateTime(startTimeStr)
                } else null

                val endTime = if (endTimeStr.isNotEmpty()) {
                    parseFlexibleDateTime(endTimeStr)
                } else null

                val imageBase64 = tokens.getOrNull(9)?.takeIf { it.isNotEmpty() }

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
                    status = status,
                    // Ha mindenképp LocalDateTime kell, de parse-ban null jöhet,
                    // fallback-ként tehetsz pl. LocalDateTime.MIN:
                    startTime = startTime ?: LocalDateTime.MIN,
                    endTime   = endTime   ?: LocalDateTime.MAX,
                    imageBase64 = imageBase64 ?: ""
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
    override fun saveTasks(tasks: List<Task>): Int {
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
                    else -> "2"
                }

                // A startTime és endTime-t legjobb "yyyy-MM-ddTHH:mm" formátumban menteni,
                // hogy később gond nélkül LocalDateTime.parse(...)-al be tudd olvasni.
                // Ha "csak óra:perc" formátumot akarsz, pl. "HH:mm", akkor
                // parseFlexibleDateTime(...) logikát kell fenntartanod.

                val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val stStr = task.startTime?.format(fmt) ?: ""
                val etStr = task.endTime?.format(fmt)   ?: ""

                "${task.id}|" +
                        "${escapeField(task.title)}|" +
                        "${escapeField(task.priority.toString())}|" +
                        "${task.createdAt.format(fmt)}|" +
                        "${task.updatedAt.format(fmt)}|" +
                        "${task.deadline?.format(fmt) ?: ""}|" +
                        "$statusValue|" +
                        stStr + "|" +
                        etStr + "|" +
                        (task.imageBase64 ?: "")
            }

            println(lines)
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
    override fun updateTask(taskId: Int, updatedData: Task): Int {
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
    override fun addTask(newTask: Task): Int {
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

    /**
     * Löscht eine Aufgabe anhand der ID.
     * @param taskId Die ID der zu löschenden Aufgabe.
     * @return 200 bei Erfolg, 404 wenn keine Aufgabe mit der ID gefunden wurde, 400 bei anderen Fehlern.
     */
    override fun deleteTaskById(taskId: Int): Int {
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
    override fun updateTaskPriority(taskId: Int, newPriority: Enum<Priority>): Int {
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

    override fun updateTaskStatus(taskId: Int, newStatus: Int?): Int {
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

    private fun parseFlexibleDateTime(value: String?): LocalDateTime? {
        if (value.isNullOrBlank()) return null

        return try {
            // 1) Próbáljuk LocalDateTime.parse(...) -> pl. "2023-10-04T17:00"
            LocalDateTime.parse(value)
        } catch (ex: Exception) {
            // 2) Ha nem sikerül, próbáljuk LocalTime.parse(...) -> pl. "17:00"
            try {
                val lt = LocalTime.parse(value)
                // Tegyük mondjuk a mai napra:
                LocalDateTime.of(LocalDate.now(), lt)
            } catch (ex2: Exception) {
                // 3) Se LocalDateTime, se LocalTime nem parse-olható -> null
                null
            }
        }
    }

    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }

}
