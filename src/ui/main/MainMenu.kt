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
import ui.add.AddMenu
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
        val timelineButton=Button("Zeitachse").apply {
            setOnAction{
                openZeitachse(taskService.getAllTasks())
            }
        }

        val uploadButton=Button("Add Menü").apply { setOnAction{
            openAddMenu(taskService)
        } }

        println("here")
        primaryStage.title="FocusFlow"
        val layout = VBox(20.0, uploadButton, timelineButton).apply {
            spacing = 20.0
        }
        val scene = Scene(layout, 800.0, 600.0)
        primaryStage.setScene(scene)
        primaryStage.show()
        println("app läuft...")
    }

    fun openAddMenu(taskService: TaskService){
        val addMenu = AddMenu()
        addMenu.openUploadMenu(taskService)
    }

    fun openZeitachse(tasks: List<Task>) {
        val stage = Stage()
        val zeitachse = TimeLineMenu()
        zeitachse.start(stage, tasks)
    }

}

