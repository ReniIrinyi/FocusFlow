package view.admin.AdminSettings

import javafx.scene.control.*
import javafx.scene.layout.VBox
import service.UserService

class AdminAuthSettings(
    private val userService: UserService,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        val adminUser = userService.getAdmin()

        val usernameField = TextField(adminUser?.name ?: "").apply {
            promptText = "Admin Username"
            isEditable = false // Admin-Benutzername kann nicht geändert werden
        }

        val passwordField = PasswordField().apply {
            promptText = "New Password"
        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Confirm New Password"
        }

        val saveButton = Button("Update Admin Password").apply {
            setOnAction {
                updatePassword(usernameField.text, passwordField.text, confirmPasswordField.text, adminUser?.name)
            }
        }

        val mainMenu = VBox(20.0).apply {
            children.addAll(
                Label("Benutzereinstellungen"),
                Label("Admin-Einstellungen"),
                usernameField,
                passwordField,
                confirmPasswordField,
                saveButton
            )
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }

        return mainMenu
    }

    private fun updatePassword(username: String, password: String, confirmPassword: String, adminName: String?) {
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        when {
            trimmedPassword.isEmpty() -> {
                showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort darf nicht leer sein!")
            }
            trimmedPassword.length < 6 -> {
                showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
            }
            trimmedPassword != trimmedConfirmPassword -> {
                showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
            }
            else -> {
                userService.updateAdminPassword(adminName ?: "", trimmedPassword)
                showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Passwort wurde erfolgreich aktualisiert.")
                onSettingsSaved()
            }
        }
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            this.contentText = message
            showAndWait()
        }
    }
}