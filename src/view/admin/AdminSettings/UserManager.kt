package view.admin.AdminSettings

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.User
import service.UserService
import utils.Role

class UserManager(
    private val userService: UserService,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        // ListView for displaying users
        val userList = ListView<String>().apply {
            items.addAll(userService.findAll().map { it -> "${it.name} (${it.role.name})" })
            prefHeight = 200.0 // Set a smaller height for the ListView
        }

        val userDetailsPane = VBox().apply {
            children.add(Label("Select a user to view or edit details."))
        }

        userList.setOnMouseClicked {
            val selectedUser = userList.selectionModel.selectedItem
            if (selectedUser != null) {
                val userName = selectedUser.substringBefore(" (")
                val user = userService.findAll().find { it.name == userName }
                if (user != null) {
                    val userEditPane = createBetreuterSubMenu(user)
                    userDetailsPane.children.setAll(userEditPane)
                }
            }
        }

        val addUserButton = Button("Add User").apply {
            setOnAction {
                showAddUserModal(userList)
            }
        }

        val deleteUserButton = Button("Delete User").apply {
            setOnAction {
                val selectedUser = userList.selectionModel.selectedItem
                if (selectedUser != null) {
                    val userName = selectedUser.substringBefore(" (")
                    val user = userService.findAll().find { it.name == userName }
                    if (user != null) {
                        userService.delete(user.id)
                        userList.items.remove(selectedUser)
                        userDetailsPane.children.setAll(Label("Select a user to view or edit details."))
                        showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.")
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "No user selected to delete.")
                }
            }
        }

        val buttonBox = HBox(10.0, addUserButton, deleteUserButton)

        return VBox(10.0, Label("Manage Users"), userList, buttonBox, userDetailsPane).apply {
            style = "-fx-padding: 20px; -fx-background-color: #E8F5E9; -fx-spacing: 10px;"
        }
    }

    private fun createBetreuterSubMenu(user: User): VBox {
        val nameField = TextField(user.name).apply {
            promptText = "Username"
        }

        val emailField = TextField(user.email).apply {
            promptText = "Email"
        }

        val roleDropdown = ComboBox<String>().apply {
            items.addAll("Admin", "User")
            value = user.role.name
        }

        val saveButton = Button("Save Changes").apply {
            setOnAction {
                val updatedName = nameField.text.trim()
                val updatedEmail = emailField.text.trim()
                val updatedRole = roleDropdown.value

                if (updatedName.isEmpty() || updatedEmail.isEmpty() || updatedRole.isNullOrEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled out!")
                } else if (!isValidEmail(updatedEmail)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid email format!")
                } else {
                    val user = User(user.id,updatedName,updatedEmail,user.password,Role.valueOf(updatedRole.uppercase()))
                    userService.save(user)
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User details updated successfully.")
                    onSettingsSaved()
                }
            }
        }

        return VBox(10.0, Label("Edit User"), nameField, emailField, roleDropdown, saveButton).apply {
            style = "-fx-padding: 10px; -fx-border-color: #ccc; -fx-border-radius: 5px;"
        }
    }

    private fun showAddUserModal(userList: ListView<String>) {
        val dialog = Dialog<User>().apply {
            title = "Add New User"
            dialogPane.content = VBox(10.0).apply {
                val nameField = TextField().apply { promptText = "Username" }
                val emailField = TextField().apply { promptText = "Email" }
                val passwordField = PasswordField().apply { promptText = "Password" }
                val roleDropdown = ComboBox<String>().apply {
                    items.addAll("Admin", "User")
                    promptText = "Select Role"
                }
                children.addAll(
                    Label("Add User Details"), nameField, emailField, passwordField, roleDropdown
                )
            }
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { buttonType ->
                if (buttonType == ButtonType.OK) {
                    val name = (dialogPane.content as VBox).children[1] as TextField
                    val email = (dialogPane.content as VBox).children[2] as TextField
                    val password = (dialogPane.content as VBox).children[3] as PasswordField
                    val role = (dialogPane.content as VBox).children[4] as ComboBox<*>

                    if (name.text.isNotEmpty() && email.text.isNotEmpty() && password.text.isNotEmpty() && role.value != null) {
                        User(
                            id = User.generateId(),
                            name = name.text,
                            email = email.text,
                            password = password.text,
                            role = Role.valueOf(role.value.toString().uppercase())
                        )
                    } else {
                        null
                    }
                } else null
            }
        }

        val result = dialog.showAndWait()
        result.ifPresent { user ->
            userService.save(user)
            userList.items.add("${user.name} (${user.role.name})")
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

    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"))
    }
}
