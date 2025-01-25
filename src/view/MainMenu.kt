package view

import storage.TaskStorage
import storage.TimeLineSettingsStorage
import storage.UserStorage
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.BorderPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import model.Task
import model.TimeLineSettings
import model.User
import controller.GenericController
import utils.HelperFunctions
import view.admin.AdminAuthSettings
import view.admin.Menu
import view.timeline.TimeLineManager


// Der Einstiegspunkt der Anwendung.
class MainMenu : Application() {

    private val root = BorderPane()
    private val userController = GenericController<User>(
        storage = UserStorage()
    )
    private val taskController = GenericController<Task>(
        storage = TaskStorage()
    )
    private val timeLineSettingsController = GenericController<TimeLineSettings>(
        storage = TimeLineSettingsStorage()
    )
    private val helperFunctions = HelperFunctions()
    private var isAdminLoggedIn = false


    override fun start(primaryStage: Stage) {
        val isAdminExists = userController.createRequest("GET", null, null,null,"isAdminExists").first as Boolean
        if (!isAdminExists) {
            showUserSettings()
        } else {
            setupHeader()
            showTimeLineMenu()
        }

        val scene = Scene(root)

        val css = this.javaClass.getResource("../styles/styles.css")
        if (css != null) {
            scene.stylesheets.add(css.toExternalForm())
        } else {
            println("Style.css konnte nicht gefunden werden!")
        }
        primaryStage.title = "CareFlow"
        primaryStage.scene = scene
        primaryStage.show()
    }

    private fun showUserSettings() {
        val userSettings = AdminAuthSettings(userController,helperFunctions) {
            setupHeader()
            showTimeLineMenu()
        }
        root.center = userSettings.createView()
    }

    private fun setupHeader() {
        val header = Header(
            taskController,onZeitachseClicked = { showTimeLineMenu() },
            onAdminClicked = { showAdminMenu() }
        )
        root.top = header
    }

    private fun showTimeLineMenu() {
        val timeLineMenu = TimeLineManager(taskController, userController)
        root.center = timeLineMenu.createView()
        this.isAdminLoggedIn = false
    }

    private fun showAdminMenu() {
        if (!isAdminLoggedIn) {
            if (authenticateAdmin()) {
                isAdminLoggedIn = true
            } else {
                helperFunctions.showAlert(Alert.AlertType.ERROR,"Authentication Failed", "Only the admin can access this section.")
                return
            }
        }
        val adminView = Menu(taskController, userController, timeLineSettingsController, helperFunctions)
        root.center = adminView.createView()
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
        println(username)
        println(password)
        val user = User (
            id = 0,
            name = username,
            email = "",
            password = password,
            role = 0,
            profileImage = ""
        )
        val isAdminExists = userController.createRequest("GET", null, null,null,"isAdminExists").first as Boolean
        println(isAdminExists)
        return if (isAdminExists) {
            return userController.createRequest("GET", null, null,user,"validateUser").first as Boolean
        } else {
            false
        }
    }

}
