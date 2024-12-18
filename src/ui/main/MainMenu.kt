package ui.main

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.stage.Stage
import model.Task
import service.FileHandler
import service.TaskService
import ui.timeline.TimeLineMenu

/**
 * Der Einstiegspunkt der Anwendung.
 * TODO @reni:
 * - Erstelle eine Instanz der TaskService-Klasse.
 * - Erstelle eine Instanz der MainMenu-Klasse und übergib den TaskService.
 * - Rufe die Methode `MainMenu.show()` auf, um das Hauptmenü anzuzeigen.
 */
class MainMenu: Application() {

    val id:Int = 0
    override fun start(primaryStage: Stage) {
        val taskService:TaskService = TaskService()
        val btn=Button("Zeitachse").apply {
            setOnAction{
                openZeitachse(taskService.getAllTasks())
            }
        }
        println("here")
        primaryStage.title="FocusFlow"
        var main = Group()
        main.getChildren().add( btn)
        var scene = Scene(main, 800.0, 600.0)
        primaryStage.setScene(scene)
        primaryStage.show()
        println("app läuft...")
    }

    fun openZeitachse(tasks: List<Task>) {
        val stage = Stage()
        val zeitachse = TimeLineMenu()
        zeitachse.start(stage, tasks)
    }

}

