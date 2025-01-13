package ui.AdminSettings

import UserService
import javafx.scene.control.*
import javafx.scene.layout.VBox
import model.User
import utils.Role

class UserSettings(
    private val userService: UserService,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        val adminExists = userService.getAdmin() != null

        // If no admin exists, show only the admin creation form
        if (!adminExists) {
            return VBox(20.0).apply {
                children.add(createAdminCreationForm())
                style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
            }
        }

        // Otherwise, show the full admin and Betreuter management view
        val adminUser = userService.getAdmin()

        val mainMenu = VBox(20.0).apply {
            children.addAll(
                Label("Benutzereinstellungen"),
                createAdminSettingsView(adminUser),
            )
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }

        return mainMenu
    }

    private fun createAdminCreationForm(): VBox {
        val usernameField = TextField().apply {
            promptText = "Admin Username"
        }

        val emailField = TextField().apply {
            promptText = "Admin Email"
        }

        val passwordField = PasswordField().apply {
            promptText = "Password"
        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Confirm Password"
        }

        val saveButton = Button("Create Admin").apply {
            setOnAction {
                val username = usernameField.text.trim()
                val email = emailField.text.trim()
                val password = passwordField.text.trim()
                val confirmPassword = confirmPasswordField.text.trim()

                when {
                    username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Alle Felder müssen ausgefüllt werden!")
                    }
                    !isValidEmail(email) -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Ungültige E-Mail-Adresse!")
                    }
                    password.length < 6 -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
                    }
                    password != confirmPassword -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
                    }
                    else -> {
                        userService.saveUser(username, email, password, Role.ADMIN)
                        showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Admin-Benutzer wurde erfolgreich erstellt.")
                        onSettingsSaved()
                    }
                }
            }
        }

        return VBox(20.0, Label("Erstellen Sie einen Admin-Benutzer"), usernameField, emailField, passwordField, confirmPasswordField, saveButton).apply {
            style = "-fx-padding: 20px; -fx-border-color: #ccc; -fx-border-radius: 5px;"
        }
    }

    private fun createAdminSettingsView(adminUser: User?): VBox {
        val usernameField = TextField(adminUser?.name ?: "").apply {
            promptText = "Admin Username"
            isEditable = false // Admin username cannot be changed
        }

        val passwordField = PasswordField().apply {
            promptText = "New Password"
        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Confirm New Password"
        }

        val saveButton = Button("Update Admin Password").apply {
            setOnAction {
                val password = passwordField.text.trim()
                val confirmPassword = confirmPasswordField.text.trim()

                when {
                    password.isEmpty() -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort darf nicht leer sein!")
                    }
                    password.length < 6 -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
                    }
                    password != confirmPassword -> {
                        showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
                    }
                    else -> {
                        userService.updateAdminPassword(adminUser?.name ?: "", password)
                        showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Passwort wurde erfolgreich aktualisiert.")
                        onSettingsSaved()
                    }
                }
            }
        }

        return VBox(20.0, Label("Admin-Einstellungen"), usernameField, passwordField, confirmPasswordField, saveButton)
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

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }
}
