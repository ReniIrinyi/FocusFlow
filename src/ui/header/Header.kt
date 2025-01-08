package ui.header

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import model.Task
import service.TaskService

class Header(
    val taskService: TaskService,
    val onZeitachseClicked: (List<Task>) -> Unit,
    val onAdminClicked: () -> Unit
) : HBox(10.0) {

    init {
        val timelineButton = Button("Zeitachse").apply {
            setOnAction { onZeitachseClicked(taskService.getAllTasks()) }
        }

        val adminButton = Button("Admin").apply {
            setOnAction { onAdminClicked() }
        }

        this.children.addAll(timelineButton, adminButton)
        this.style = "-fx-background-color: #F5F5F5; -fx-padding: 10px;"
    }
}