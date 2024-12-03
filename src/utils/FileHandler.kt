package utils

import model.Task
import java.io.File
import java.time.LocalDateTime

/**
 * Diese Klasse verwaltet das Speichern und Laden von Aufgaben aus einer Datei.
 *
 * Ziel:
 * - Persistente Speicherung der Aufgaben, auch nach Beenden der Anwendung.
 * - Verwendung des JSON-Formats für die Speicherung.
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
     * TODO @reni:
     * 1. Prüfe, ob die Datei existiert:
     *    - Wenn ja: Lies den Inhalt der Datei und parse ihn in eine Liste von Aufgaben.
     *    - Wenn nein: Gib eine leere Liste zurück.
     * 2. Verwende Gson für die JSON-Verarbeitung.
     *
     * @return Liste der gespeicherten Aufgaben (List<Task>).
     */
    fun loadTasks(): List<Task> {
        val file = File(filePath)

        if (!file.exists() || file.readText().isEmpty()) {
            return emptyList()
        }

        val tasks = file.readLines().map { line ->
            val tokens = line.split("|")

            // Konvertiere die Strings zurück in die entsprechenden Datentypen
            val id = tokens[0].toInt()
            val title = unescapeField(tokens[1])
            val priority = unescapeField(tokens[2])
            val createdAt = LocalDateTime.parse(tokens[3])
            val updatedAt = LocalDateTime.parse(tokens[4])
            val deadline = LocalDateTime.parse(tokens[5])
            val status = unescapeField(tokens[6])

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

        // Aktualisiere currentId mit der höchsten vorhandenen ID
        if (tasks.isNotEmpty()) {
            val maxId = tasks.maxOf { it.id }
            Task.currentId = maxId
        }

        return tasks
    }


    /**ü'
     * Speichert alle Aufgaben in der Datei.
     * TODO @reni:
     * 1. Konvertiere die Liste von Aufgaben in JSON-Format.
     * 2. Schreibe das JSON in die Datei.
     * 3. Falls die Datei nicht existiert, erstelle sie automatisch.
     *
     * @param tasks Die Liste der Aufgaben, die gespeichert werden sollen.
     */
    fun saveTasks(tasks: List<Task>) {
        val file = File(filePath)

        // Falls die Datei nicht existiert, erstelle sie automatisch
        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        val lines = tasks.map { task ->
            // Konvertiere jedes Feld in einen String und verbinde sie mit dem Trennzeichen
            "${task.id}|${escapeField(task.title)}|${escapeField(task.priority)}|${task.createdAt}|${task.updatedAt}|${task.deadline}|${escapeField(task.status)}"
        }

        // Schreibe alle Zeilen in die Datei
        file.writeText(lines.joinToString("\n"))
    }

    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }


}
