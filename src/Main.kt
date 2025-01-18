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
    Application.launch(MainMenu::class.java)
}
