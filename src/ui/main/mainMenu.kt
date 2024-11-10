package ui.main

import service.TaskService

/**
 * Der Einstiegspunkt der Anwendung.
 * TODO @reni:
 * - Erstelle eine Instanz der TaskService-Klasse.
 * - Erstelle eine Instanz der MainMenu-Klasse und übergib den TaskService.
 * - Rufe die Methode `MainMenu.show()` auf, um das Hauptmenü anzuzeigen.
 */
class mainMenu (private val taskService:TaskService) {
    fun init(){
        taskService.init()
        println("init MainMenu... ")
    }
}