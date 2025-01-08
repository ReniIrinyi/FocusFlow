package ui.main

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextInputDialog
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import service.TaskService
import service.UserService
import ui.adminMenu.AdminMenu
import ui.header.Header
import ui.timeline.TimeLineMenu
import ui.usersettings.UserSettings

// Der Einstiegspunkt der Anwendung.
class MainMenu: Application() {

    private val root = BorderPane()
    private val userService = UserService()
    private val taskService = TaskService()

    override fun start(primaryStage: Stage) {
        if (!userService.userExists()) {
            showUserSettings()
        } else {
            setupHeader()
            showZeitachse()
        }

        val scene = Scene(root, 1200.0, 800.0)
        primaryStage.title = "FocusFlow"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun setupHeader() {
        val header = Header(
            taskService = taskService,
            onZeitachseClicked = { showZeitachse() },
            onAdminClicked = { showAdminView() }
        )
        header.style = "-fx-background-color: rgba(255,0,0,0.3);"

        root.top = header
    }

    private fun showZeitachse() {
        val timeLineMenu = TimeLineMenu()
        root.center = timeLineMenu.createView(taskService.getAllTasks())
    }

    private fun showUserSettings() {
        val userSettings = UserSettings(userService) {
            setupHeader()
            showZeitachse()
        }
        root.center = userSettings.createView()
    }

    private fun showAdminView() {
        if (authenticate()) {
            val adminView = AdminMenu(taskService, userService)
            root.center = adminView.createView()
        }
    }

    private fun authenticate(): Boolean {
        val dialog = TextInputDialog().apply {
            title = "Authenticate"
            headerText = "Password: "
        }
        val result = dialog.showAndWait()
        return result.isPresent && userService.validateUser(result.get())
    }
}

