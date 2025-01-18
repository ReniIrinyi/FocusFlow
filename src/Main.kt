import javafx.application.Application
import view.MainMenu

/**
 * Einstiegspunkt der Anwendung.
 *
 * Ablauf:
 * - Initialisiert den `TaskService`, der die Aufgaben verwaltet.
 * - Startet das Hauptmenü (`MainMenu`), über das der Benutzer alle Funktionen erreichen kann.
 */
fun main() {

    ///////////////////////test FileHandler////////////////////////////////
      /*val fileHandler = FileHandler()
        val tasks = listOf(
        Task(
            id = Task.generateId(),
            title = "Projektplan aktualisieren",
            priority = "PRIORITY_HIGH",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = LocalDateTime.now().plusDays(5),
            status = Constants.STATUS_DONE.toString(),
            startTime = LocalTime.of(17, 0),
            endTime = LocalTime.of(21, 0),

        ),
        Task(
            id = Task.generateId(),
            title = "Teammeeting vorbereiten",
            priority = "PRIORITY_MEDIUM",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = LocalDateTime.now().plusDays(2),
            status = Constants.STATUS_IN_PROGRESS.toString(),
            startTime = LocalTime.of(16, 0),
            endTime = LocalTime.of(17, 0)
        ),
        Task(
            id = Task.generateId(),
            title = "Bericht schreiben",
            priority = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = null, // Keine Deadline
            status = Constants.STATUS_NOT_DONE.toString(),
            startTime = LocalTime.of(19, 0),
            endTime = LocalTime.of(23, 0)
        )
    )
        fileHandler.saveTasks(tasks)
    fileHandler.loadTasks()*/
    //val taskService = TaskService()
   // var showMenu = ShowMenu(taskService, 1);
    ///////////////////////////////////////////////////////////////////////
    Application.launch(MainMenu::class.java)
}
