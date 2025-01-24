package view

import javafx.scene.control.*
import model.Task

import javafx.scene.layout.VBox
import controller.GenericController

class Header(
    private val taskController: GenericController<Task>,
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
            val tasks = taskController.createRequest("GET", null, null, null, "all").first
            println(tasks.toString())
            setOnAction { onZeitachseClicked(tasks as List<Task>) }
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
