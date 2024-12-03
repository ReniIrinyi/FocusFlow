import model.Task
import service.TaskService
import ui.main.MainMenu
import utils.FileHandler
import java.time.LocalDateTime

/**
 * Einstiegspunkt der Anwendung.
 *
 * Ablauf:
 * - Initialisiert den `TaskService`, der die Aufgaben verwaltet.
 * - Startet das Hauptmenü (`MainMenu`), über das der Benutzer alle Funktionen erreichen kann.
 */
fun main() {
   val taskService = TaskService()
   val mainMenu = MainMenu(taskService)
    mainMenu.init()

    ///////////////////////77test FileHandler////////////////////////////////
        val fileHandler = FileHandler()
        val tasks = listOf(
            Task(
                id = Task.generateId(),
                title = "Projektplan aktualisieren",
                priority = "Hoch",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                deadline = LocalDateTime.now().plusDays(5),
                status = "Erstellt"
            ),
            Task(
                id = Task.generateId(),
                title = "Teammeeting vorbereiten",
                priority = "Mittel",
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                deadline = LocalDateTime.now().plusDays(2),
                status = "Erstellt"
            )
        )

        fileHandler.saveTasks(tasks)
        println("Aufgaben wurden erfolgreich gespeichert.")
        // Aufgaben laden
        val loadedTasks = fileHandler.loadTasks()
        println("Geladene Aufgaben:")
        loadedTasks.forEach { println(it) }
    ///////////////////////////////////////////////////////////////////////
}
