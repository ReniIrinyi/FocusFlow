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
        spacing = 10.0

        val menuBar = MenuBar().apply {
            styleClass.add("header")
        }

        val adminMenu = Menu("Admin").apply {
            showingProperty().addListener { _, _, isShowing ->
                if (isShowing) {
                    onAdminClicked()
                }
            }
        }

        val openTimeline = MenuItem("Zeitachse").apply {
            val tasks = taskController.read(null, null, null, "all").first
            setOnAction { onZeitachseClicked(tasks as List<Task>) }
        }

        val openAdminPanel = MenuItem("Adminbereich").apply {
            setOnAction { onAdminClicked() }
        }

        val about = MenuItem("CareFlow").apply {
            setOnAction { showAboutDialog() }
        }

        adminMenu.items.addAll(openAdminPanel, openTimeline, about)
        menuBar.menus.add(adminMenu)

        this.children.add(menuBar)
    }

    private fun showAboutDialog() {
        val alert = Alert(Alert.AlertType.INFORMATION)
        alert.title = "CareFlow"
        alert.headerText = "Version: 0.5"
        alert.contentText = "Letzte Aktualisierung: 14.02.2025"
        alert.showAndWait()
    }
}
