package service

import model.Task

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
     * Konstruktor, der automatisch alle gespeicherten Aufgaben lädt.
     */
    init {
        init() // Initialisiert den TaskService und lädt die gespeicherten Aufgaben.
    }

    /**
     * Initialisiert den TaskService und lädt alle gespeicherten Aufgaben.
     * Diese Methode wird im Konstruktor automatisch aufgerufen.
     */
    fun init() {
        tasks.clear()
        val (loadedTasks, status) = loadTasks()
        handleFileHandlerResponse(
            status,
            successMessage = "TaskService erfolgreich initialisiert. ${loadedTasks.size} Aufgaben geladen.",
            failureMessage = "Fehler beim Laden der Aufgaben."
        ) {
            tasks.addAll(loadedTasks)
        }
    }

    /**
     * Gibt alle vorhandenen Aufgaben zurück.
     * @return Liste aller Aufgaben (List<Task>).
     */
    fun getAllTasks(): List<Task> {
        return tasks.toList()
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
    fun add(newTask: Task) {
      var status = addTask(newTask)
  }

    /**
     * Fügt eine neue Aufgabe hinzu.
     * TODO @alex:
     * 1. Füge die neue Aufgabe (`task`) zur `tasks`-Liste hinzu.
     * 2. Speichere die aktualisierte Liste mit `fileHandler.saveTasks(tasks)`.
     *
     * @param task Die neue Aufgabe, die hinzugefügt werden soll.
     */
   fun update(taskId: Int, updatedTask: Task) {
     var status =  updateTask(taskId,updatedTask)
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
    fun delete(taskId:Int) {
        var status = deleteTaskById(taskId)
        handleFileHandlerResponse(status,"", "",
            {
            println("onSucess")
        }
        )
    }

    /**
     * Behandelt die Rückgabewerte des FileHandler und gibt eine Erfolgsmeldung aus.
     * Bei Erfolg wird die übergebene `onSuccess`-Aktion ausgeführt.
     *
     * @param status Der Statuscode vom FileHandler.
     * @param successMessage Die Nachricht, die bei Erfolg ausgegeben werden soll.
     * @param failureMessage Die Nachricht, die bei einem Fehler ausgegeben werden soll.
     * @param onSuccess Eine optionale Aktion, die bei Erfolg ausgeführt werden soll.
     * @return Boolean, ob die Operation erfolgreich war.
     */
    private fun handleFileHandlerResponse(
        status: Int,
        successMessage: String,
        failureMessage: String,
        onSuccess: (() -> Unit)? = null
    ): Boolean {
        return if (status == 200) {
            println(successMessage)
            onSuccess?.invoke()
            true
        } else {
            println(failureMessage)
            false
        }
    }
}
