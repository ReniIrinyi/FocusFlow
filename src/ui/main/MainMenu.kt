package ui.main

import javafx.application.Application
import javafx.stage.Stage
import service.TaskService

/**
 * Der Einstiegspunkt der Anwendung.
 * TODO @reni:
 * - Erstelle eine Instanz der TaskService-Klasse.
 * - Erstelle eine Instanz der MainMenu-Klasse und übergib den TaskService.
 * - Rufe die Methode `MainMenu.show()` auf, um das Hauptmenü anzuzeigen.
 */
class MainMenu: Application() {

    val id:Int = 0
    override fun start(primaryStage: Stage?) {
        val taskService:TaskService = TaskService()
        println("app läuft...")
    }
}