package view

import javafx.scene.control.*
import javafx.scene.layout.HBox
import model.Task
import service.TaskService

import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class Header(
    private val taskService: TaskService,
    private val onZeitachseClicked: (List<Task>) -> Unit,
    private val onAdminClicked: () -> Unit
) : VBox() {

    init {
        styleClass.add("header")
        spacing = 10.0

        val menuBar = MenuBar().apply {
            styleClass.add("menu-bar")
        }

        val adminMenu = Menu("Admin").apply {
            // Hier wird die onAdminClicked Methode aufgerufen, wenn das Menü geöffnet wird
            showingProperty().addListener { _, _, isShowing ->
                if (isShowing) {
                    onAdminClicked()
                }
            }
        }

        val openTimeline = MenuItem("Open Timeline").apply {
            setOnAction { onZeitachseClicked(taskService.findAll()) }
        }

        val openAdminPanel = MenuItem("Admin Panel").apply {
            setOnAction { onAdminClicked() }
        }

        val about = MenuItem("About").apply {
            setOnAction { showAboutDialog() }
        }

        adminMenu.items.addAll(openAdminPanel, openTimeline, about)
        menuBar.menus.add(adminMenu)

        this.children.add(menuBar)
    }

    private fun showAboutDialog() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "About"
        alert.headerText = "FocusFlow"
        alert.contentText = "Version: 0.2"
        alert.showAndWait()
    }
}
