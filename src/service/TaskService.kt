package service

import model.Task
import utils.FileHandler

/**
 * Diese Klasse verwaltet die Geschäftslogik der Aufgaben.
 * - Nutzt FileHandler für die persistente Speicherung der Aufgaben.
 * - Enthält die Logik für:
 *   - Abrufen von Aufgaben
 *   - Hinzufügen neuer Aufgaben
 *   - Aktualisieren vorhandener Aufgaben
 *   - Löschen von Aufgaben
 */
class TaskService(): FileHandler() {

    private val tasks = mutableListOf<Task>() // Lokale Liste der Aufgaben.

    /**
     * Initialisiert den TaskService und lädt alle gespeicherten Aufgaben.
     * TODO:
     * 1. Verwende `fileHandler.loadTasks()`, um die gespeicherten Aufgaben zu laden.
     * 2. Füge die geladenen Aufgaben der lokalen `tasks`-Liste hinzu.
     */
    fun init() {
        tasks.clear()
        tasks.addAll(loadTasks())
        println("TaskService erfolgreich initialisiert.")
    }

    /**
     * Gibt alle vorhandenen Aufgaben zurück.
     * TODO @reni:
     * 1. Gib die `tasks`-Liste zurück.
     * 2. Stelle sicher, dass die Liste unverändert bleibt (immutable Rückgabe).
     *
     * @return Liste aller Aufgaben (List<Task>).
     */
    fun getAllTasks(): List<Task> {
        return tasks.toList() // Eine unveränderbare Kopie der Liste zurückgeben.
    }

    /**
     * Gibt die Aufgabe mit der übergebenen ID zurück.
     * TODO @david:
     * 1. Suche die Aufgabe in der `tasks`-Liste, deren ID mit `taskId` übereinstimmt.
     * 2. Wenn die Aufgabe gefunden wird, gib sie zurück.
     * 3. Wenn keine Aufgabe gefunden wird, Errormeldung.
     *
     * @param taskId Die ID der gesuchten Aufgabe.
     * @return Die Aufgabe mit der entsprechenden ID (Task).
     */
    fun getTask(taskId: Int): Task {
        return return {} as Task
    }

    /**
     * Aktualisiert die Aufgabe mit der übergebenen ID.
     * TODO @david:
     * 1. Suche die Aufgabe in der `tasks`-Liste anhand der ID (`taskId`).
     * 2. Wenn die Aufgabe gefunden wird:
     *    - Aktualisiere die Aufgabe mit den neuen Werten (`updatedTask`).
     *    - Speichere die aktualisierte Liste mit `fileHandler.saveTasks(tasks)`.
     * 3. Wenn die Aufgabe nicht gefunden wird, wirf eine Ausnahme.
     *
     * @param taskId Die ID der Aufgabe, die aktualisiert werden soll.
     * @param updatedTask Neue Daten für die Aufgabe (Task).
     * @return Die aktualisierte Aufgabe (Task).
     */
    fun updateTask(taskId: Int, updatedTask: Task): Task {
        return {} as Task
    }

    /**
     * Fügt eine neue Aufgabe hinzu.
     * TODO @alex:
     * 1. Füge die neue Aufgabe (`task`) zur `tasks`-Liste hinzu.
     * 2. Speichere die aktualisierte Liste mit `fileHandler.saveTasks(tasks)`.
     *
     * @param task Die neue Aufgabe, die hinzugefügt werden soll.
     */
    fun addTask(task: Task) {
    }

    /**
     * Löscht eine Aufgabe.
     * TODO @alex:
     * 1. Entferne die Aufgabe (`task`) aus der `tasks`-Liste.
     * 2. Wenn die Aufgabe erfolgreich entfernt wurde:
     *    - Speichere die aktualisierte Liste mit `fileHandler.saveTasks(tasks)`.
     * 3. Wenn die Aufgabe nicht gefunden wird, Errormeldung.
     *
     * @param task Die Aufgabe, die gelöscht werden soll.
     */
    fun deleteTask(task: Task) {
    }
}
