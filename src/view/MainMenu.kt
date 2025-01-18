package view

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import service.TaskService
import service.TimeLineSettingsService
import service.UserService
import utils.HelperFunctions
import view.admin.AdminSettings.AdminAuthSettings
import view.admin.AdminMenu
import view.timeline.TimeLineManager


// Der Einstiegspunkt der Anwendung.
class MainMenu : Application() {

    private val root = BorderPane()
    private val userService = UserService()
    private val taskService = TaskService()
    private val timeLineSettingsService = TimeLineSettingsService()
     private val helperFunctions = HelperFunctions()

    override fun start(primaryStage: Stage) {
        if (!userService.isAdminExists()) {
            showUserSettings()
        } else {
            setupHeader()
            showTimeLineMenu()
        }

        val scene = Scene(root, 1200.0, 800.0)
        primaryStage.title = "FocusFlow"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun showUserSettings() {
        val userSettings = AdminAuthSettings(userService,helperFunctions) {
            setupHeader()
            showTimeLineMenu()
        }
        root.center = userSettings.createView()
    }

    private fun setupHeader() {
        val header = Header(
            taskService,onZeitachseClicked = { showTimeLineMenu() },
            onAdminClicked = { showAdminMenu() }
        )
        header.style = "-fx-background-color: rgba(255,0,0,0.3);"

        root.top = header
    }

    private fun showTimeLineMenu() {
        val timeLineMenu = TimeLineManager(taskService, userService,helperFunctions)
        root.center = timeLineMenu.createView()
    }

    private fun showAdminMenu() {
        if (authenticateAdmin()) {
            val adminView = AdminMenu(taskService, userService, timeLineSettingsService,helperFunctions)
            root.center = adminView.createView()
        } else {
            helperFunctions.showAlert((Alert.AlertType.ERROR),"Authentication Failed", "Only the admin can access this section.")
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

}
