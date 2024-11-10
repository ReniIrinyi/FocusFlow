import service.TaskService
import ui.main.mainMenu

/**
 * Einstiegspunkt der Anwendung.
 *
 * Beschreibung:
 * - Dieses Programm ist eine einfache Aufgabenverwaltung (To-Do-Liste).
 * - Funktionen:
 *   - Aufgaben hinzufügen, anzeigen, bearbeiten und löschen.
 *   - Verwaltung von Aufgaben mit Priorität und Fälligkeitsdatum.
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
