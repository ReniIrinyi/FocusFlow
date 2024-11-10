import service.TaskService
import ui.main.mainMenu

/**
 * Einstiegspunkt der Anwendung.
 *
 * Hauptkomponenten:
 * - `TaskService`: Verarbeitet die Geschäftslogik der Aufgabenverwaltung.
 * - `MainMenu`: Die Hauptschnittstelle für den Benutzer, um das Programm zu bedienen.
 *
 * Ablauf:
 * - Initialisiert den `TaskService`, der die Aufgaben verwaltet.
 * - Startet das Hauptmenü (`MainMenu`), über das der Benutzer alle Funktionen erreichen kann.
 */
fun main() {
   val taskService = TaskService()
   val mainMenu = mainMenu(taskService)
    mainMenu.init()
}
