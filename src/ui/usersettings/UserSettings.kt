package ui.usersettings

import javafx.scene.control.*
import javafx.scene.layout.VBox
import service.UserService

class UserSettings(
    private val userService: UserService,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        val usernameField = TextField().apply {
            promptText = "Username"
        }

        val passwordField = PasswordField().apply {
            promptText = "Password"
        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Password confirmation"
        }

        val saveButton = Button("Save").apply {
            setOnAction {
                val username = usernameField.text
                val password = passwordField.text
                val confirmPassword = confirmPasswordField.text

                if (username.isEmpty() || password.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Fehler", "Alle Felder müssen ausgefüllt werden")
                } else if (!isValidUsername(username)) {
                    showAlert(Alert.AlertType.ERROR, "Fehler", "Der Benutzername ist ungültig! (Darf keine Sonderzeichen enthalten)")
                } else if (password.length < 6) {
                    showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
                } else if (password != confirmPassword) {
                    showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
                } else {
                    userService.saveUser(username, password)
                    showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Benutzerdaten wurden erfolgreich gespeichert.")
                    onSettingsSaved()
                }
            }
        }

        return VBox(20.0, Label("Benutzereinstellungen"), usernameField, passwordField, confirmPasswordField, saveButton).apply {
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }
    }

    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            this.contentText = message
            showAndWait()
        }
    }

    private fun isValidUsername(username: String): Boolean {
        return username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }
}
