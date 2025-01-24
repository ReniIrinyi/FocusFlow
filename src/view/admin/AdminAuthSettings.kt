package view.admin

import javafx.scene.control.*
import javafx.scene.layout.VBox
import model.User
import controller.GenericController
import utils.HelperFunctions

class AdminAuthSettings(
    private val userController: GenericController<User>,
    private val helperFunctions: HelperFunctions,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        val admin = userController.createRequest("GET", null, null,null,"getAdmin").first as User

        return if (admin != null) {
            createAdminPasswordUpdateView(admin.name)
        } else {
            createNewAdminView()
        }
    }

    private fun createNewAdminView(): VBox {
        val nameField = TextField().apply {
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
                createNewAdmin(nameField.text, emailField.text, passwordField.text, confirmPasswordField.text)
            }
        }

        return VBox(20.0).apply {
            children.addAll(
                Label("Admin Registration"),
                nameField,
                emailField,
                passwordField,
                confirmPasswordField,
                saveButton
            )
        }
    }

    private fun createAdminPasswordUpdateView(adminName: String): VBox {
        val usernameField = TextField(adminName).apply {
            promptText = "Admin Username"
            isEditable = false
        }

        val passwordField = PasswordField().apply {
            promptText = "New Password"
        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Confirm New Password"
        }

        val saveButton = Button("Update Admin Password").apply {
            setOnAction {
                updateAdminPassword(passwordField.text, confirmPasswordField.text)
            }
        }

        return VBox(20.0).apply {
            children.addAll(
                Label("Passwort ändern"),
                usernameField,
                passwordField,
                confirmPasswordField,
                saveButton
            )

        }
    }

    private fun createNewAdmin(username: String, email: String, password: String, confirmPassword: String) {
        val trimmedUsername = username.trim()
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        when {
            trimmedUsername.isEmpty() -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Der Benutzername darf nicht leer sein!")
            }
            trimmedEmail.isEmpty() -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Die E-Mail-Adresse darf nicht leer sein!")
            }
            !isValidEmail(trimmedEmail) -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Die angegebene E-Mail-Adresse ist ungültig!")
            }
            trimmedPassword.isEmpty() -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort darf nicht leer sein!")
            }
            trimmedPassword.length < 6 -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
            }
            trimmedPassword != trimmedConfirmPassword -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
            }
            else -> {
                val newUser  = User(
                    id = User.generateId(),
                    name = trimmedUsername,
                    email = trimmedEmail,
                    password = trimmedPassword,
                    role = 1,
                    profileImage = "",
                )
                userController.createRequest("POST", null, null,newUser,null)
                helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Admin-Benutzer wurde erfolgreich erstellt.")
                onSettingsSaved()
            }
        }
    }

    private fun updateAdminPassword(password: String, confirmPassword: String) {
        val trimmedPassword = password.trim()
        val trimmedConfirmPassword = confirmPassword.trim()

        when {
            trimmedPassword.isEmpty() -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort darf nicht leer sein!")
            }
            trimmedPassword.length < 6 -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Das Passwort muss mindestens 6 Zeichen lang sein!")
            }
            trimmedPassword != trimmedConfirmPassword -> {
                helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Die Passwörter stimmen nicht überein!")
            }
            else -> {
                val admin = userController.createRequest("GET", null, null,null,"getAdmin").first as User
                val newUser = admin.copy(password = trimmedPassword) as User
                userController.createRequest("PUT", null, newUser.id,newUser,null)
                helperFunctions.showAlert (Alert.AlertType.INFORMATION, "Erfolg", "Passwort wurde erfolgreich aktualisiert.")
                onSettingsSaved()
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }
}