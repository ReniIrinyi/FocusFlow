import javafx.application.Application
import model.Task
import service.FileHandler
import ui.main.MainMenu
import utils.Constants
import utils.Priority
import java.time.LocalDateTime

/**
 * Einstiegspunkt der Anwendung.
 *
 * Ablauf:
 * - Initialisiert den `TaskService`, der die Aufgaben verwaltet.
 * - Startet das Hauptmenü (`MainMenu`), über das der Benutzer alle Funktionen erreichen kann.
 */
fun main() {

    ///////////////////////test FileHandler////////////////////////////////
      val fileHandler = FileHandler()
        val tasks = listOf(
        Task(
            id = Task.generateId(),
            title = "Projektplan aktualisieren",
            priority = "PRIORITY_HIGH",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = LocalDateTime.now().plusDays(5),
            status = Constants.STATUS_DONE.toString() // Erledigt
        ),
        Task(
            id = Task.generateId(),
            title = "Teammeeting vorbereiten",
            priority = "PRIORITY_MEDIUM",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = LocalDateTime.now().plusDays(2),
            status = Constants.STATUS_IN_PROGRESS.toString() // In Bearbeitung
        ),
        Task(
            id = Task.generateId(),
            title = "Bericht schreiben",
            priority = "",
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            deadline = null, // Keine Deadline
            status = Constants.STATUS_NOT_DONE.toString() // Nicht erledigt
        )
    )
        fileHandler.saveTasks(tasks)
    fileHandler.loadTasks()
    ///////////////////////////////////////////////////////////////////////
    Application.launch(MainMenu::class.java)
}
