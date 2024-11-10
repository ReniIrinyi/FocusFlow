package utils

import model.Task

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
class FileHandler {

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
        println("FileHandler ladet die Todos... ")
        return emptyList()
    }

    /**
     * Speichert alle Aufgaben in der Datei.
     * TODO @reni:
     * 1. Konvertiere die Liste von Aufgaben in JSON-Format.
     * 2. Schreibe das JSON in die Datei.
     * 3. Falls die Datei nicht existiert, erstelle sie automatisch.
     *
     * @param tasks Die Liste der Aufgaben, die gespeichert werden sollen.
     */
    fun saveTasks(tasks: List<Task>) {
    }
}
