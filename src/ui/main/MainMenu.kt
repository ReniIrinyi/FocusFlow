package ui.main

import UserService
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import service.TaskService
import ui.AdminSettings.UserSettings
import ui.adminMenu.AdminMenu
import ui.header.Header
import ui.timeline.TimeLineMenu


// Der Einstiegspunkt der Anwendung.
class MainMenu : Application() {

    private val root = BorderPane()
    private val userService = UserService()
    private val taskService = TaskService()

    override fun start(primaryStage: Stage) {
        if (userService.getUsers().isEmpty()) {
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
        if (authenticateAdmin()) {
            val adminView = AdminMenu(taskService, userService)
            root.center = adminView.createView()
        } else {
            showAlert("Authentication Failed", "Only the admin can access this section.")
        }
    }

    /**
     * Authenticate the admin user by prompting for username and password.
     */
    private fun authenticateAdmin(): Boolean {
        val dialog = Dialog<Pair<String, String>>().apply {
            title = "Admin Authentication"
            headerText = "Enter Admin Username and Password"

            val usernameField = TextField().apply { promptText = "Username" }
            val passwordField = PasswordField().apply { promptText = "Password" }

            val dialogPane = dialogPane
            dialogPane.content = VBox(10.0, Label("Username:"), usernameField, Label("Password:"), passwordField)
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { buttonType ->
                if (buttonType == ButtonType.OK) {
                    usernameField.text to passwordField.text
                } else null
            }
        }

        val result = dialog.showAndWait()
        if (!result.isPresent) return false

        val (username, password) = result.get()

        // Validate the admin credentials
        return if (userService.isAdminExists()) {
            userService.validateUser(username, password)
        } else {
            false
        }
    }


    private fun showAlert(title: String, message: String) {
        Alert(Alert.AlertType.ERROR).apply {
            this.title = title
            this.headerText = null
            this.contentText = message
            showAndWait()
        }
    }
}
