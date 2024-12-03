package ui.main

import service.TaskService

/**
 * Der Einstiegspunkt der Anwendung.
 * TODO @reni:
 * - Erstelle eine Instanz der TaskService-Klasse.
 * - Erstelle eine Instanz der MainMenu-Klasse und übergib den TaskService.
 * - Rufe die Methode `MainMenu.show()` auf, um das Hauptmenü anzuzeigen.
 */
class MainMenu (private val taskService:TaskService) {

     val id:Int = 0
    fun init(){
        taskService.init()
        println("init MainMenu... ")
    }
}