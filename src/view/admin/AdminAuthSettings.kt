package view.admin

import javafx.scene.control.*
import javafx.scene.layout.VBox
import model.User
import controller.GenericController
import utils.Constants
import utils.HelperFunctions

class AdminAuthSettings(
    private val userController: GenericController<User>,
    private val helperFunctions: HelperFunctions,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        val response = userController.read( null, null,null,"getAdmin")
        if(response.second == Constants.STATUS_OK){
            println(response)
            val admin = response.first as User
            return createAdminPasswordUpdateView(admin.name)
        }
          return createNewAdminView()
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
            styleClass.add("input")

        }

        val passwordField = PasswordField().apply {
            promptText = "New Password"
            styleClass.add("input")

        }

        val confirmPasswordField = PasswordField().apply {
            promptText = "Confirm New Password"
            styleClass.add("input")

        }

        val saveButton = Button("Update Admin Password").apply {
            styleClass.add("custom-button")
            setOnAction {
                updateAdminPassword(passwordField.text, confirmPasswordField.text)
            }
        }

        return VBox(20.0).apply {
            styleClass.add("grid-element")
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
                userController.create(newUser)
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
                val response = userController.read( null, null,null,"getAdmin")
                if(response.second == Constants.STATUS_OK){
                    val user = (response.first as User).copy(password = trimmedPassword)
                    val saved = userController.update( user.id, user,"updatePasswort")
                    if(saved.second == Constants.STATUS_OK){
                        helperFunctions.showAlert (Alert.AlertType.INFORMATION, "Erfolg", "Passwort wurde erfolgreich aktualisiert.")
                        onSettingsSaved()
                    }
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }
}