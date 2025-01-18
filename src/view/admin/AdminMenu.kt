package view.admin

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import service.TaskService
import service.UserService
import view.admin.AdminSettings.TimelineSettings
import view.admin.AdminSettings.UserManager
import view.admin.AdminSettings.AdminAuthSettings
import view.admin.AdminSettings.TaskManager

class AdminMenu(private val taskService: TaskService, private val userService: UserService) {

    private val content = StackPane()

    fun createView(): VBox {

        val header = createAdminSettingsHeader()
        return VBox(20.0, header, HBox(20.0, content)).apply {
            style = "-fx-padding: 20px; -fx-background-color: #E8F5E9;"
        }
    }

    private fun createAdminSettingsHeader(): HBox {
        val userSettingsButton = Button("Einstellungen Admin").apply {
            setOnAction {
                val userSettings = AdminAuthSettings(userService) {
                    refreshView()
                }
                content.children.setAll(userSettings.createView())
            }
        }
        val userManagerButton = Button("Benutzer Verwalten").apply {
            setOnAction {
                val userManager = UserManager(userService) {
                    refreshView()
                }
                content.children.setAll(userManager.createView())
            }
        }

        val timelineSettingsButton = Button("Einstellungen der Zeitachse").apply {
            setOnAction {
                val timelineSettings = TimelineSettings(userService)
                content.children.setAll(timelineSettings.createView())
            }
        }

        val taskManagerButton = Button("Aufgaben Verwalten").apply {
            setOnAction {
                val taskManager = TaskManager(taskService, userService)
                content.children.setAll(taskManager.createView())
            }
        }

        return HBox(10.0,taskManagerButton, userSettingsButton, userManagerButton,timelineSettingsButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #ECECEC; -fx-spacing: 15px;"
        }
    }

    private fun refreshView() {
        content.children.clear()
    }
}
