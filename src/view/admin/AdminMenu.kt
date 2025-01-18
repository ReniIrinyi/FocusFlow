package view.admin

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import service.TaskService
import service.UserService
import view.admin.AdminSettings.TimelineSettings
import view.admin.AdminSettings.UserManager
import view.admin.AdminSettings.UserSettings
import view.timeline.MultiTimeLineManager
import view.admin.AdminSettings.AddMenu

class AdminMenu(private val taskService: TaskService, private val userService: UserService) {

    private val content = StackPane()

    fun createView(): VBox {
        // Create Header with Admin Settings options
        val header = createAdminSettingsHeader()

        // Sidebar Buttons
        val addTaskButton = Button("Add Task").apply {
            setOnAction {
                val addMenu = AddMenu()
                content.children.setAll(addMenu.createView(taskService, userService))
            }
        }

        val multiTimelineButton = Button("Show Timelines").apply {
            setOnAction {
                val multiTimeLineManager = MultiTimeLineManager(taskService, userService)
                content.children.setAll(multiTimeLineManager.createView())
            }
        }

        // Sidebar layout
        val sidebar = VBox(10.0, addTaskButton, multiTimelineButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #F5F5F5;"
        }

        // Combine Header, Sidebar, and Content
        return VBox(20.0, header, HBox(20.0, sidebar, content)).apply {
            style = "-fx-padding: 20px; -fx-background-color: #E8F5E9;"
        }
    }

    private fun createAdminSettingsHeader(): HBox {
        val userSettingsButton = Button("Benutzereinstellungen").apply {
            setOnAction {
                val userSettings = UserSettings(userService) {
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


        // Header layout with buttons
        return HBox(10.0, userSettingsButton, userManagerButton,timelineSettingsButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #ECECEC; -fx-spacing: 15px;"
        }
    }

    private fun refreshView() {
        content.children.clear()
    }
}
