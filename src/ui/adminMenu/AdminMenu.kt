package ui.adminMenu

import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import service.TaskService
import service.UserService
import ui.add.AddMenu
import ui.usersettings.UserSettings


class AdminMenu(private val taskService: TaskService, private val userService: UserService) {

    private val content = StackPane()

    fun createView(): VBox {
        val userSettingsButton = Button("User Settings").apply {
            setOnAction {
                val userSettings = UserSettings(userService) {
                    refreshView()
                }
                content.children.setAll(userSettings.createView())
            }
        }

        val addTaskButton = Button("Add Task").apply {
            setOnAction {
                val addMenu = AddMenu()
                content.children.setAll(addMenu.createView(taskService))
            }
        }

        val adminMenuButtons = VBox(10.0, userSettingsButton, addTaskButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #F5F5F5;"
        }

        return VBox(20.0, adminMenuButtons, content).apply {
            style = "-fx-padding: 20px; -fx-background-color: #E8F5E9;"
        }
    }

    private fun refreshView() {
        content.children.clear()
    }
}