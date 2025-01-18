package view.admin.AdminSettings

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.User
import service.UserService
import utils.HelperFunctions

class UserManager(
    private val userService: UserService,
    private val helperFunctions: HelperFunctions,
    private val onSettingsSaved: () -> Unit
) {

    fun createView(): VBox {
        // ListView for displaying users
        val userList = ListView<String>().apply {
            items.addAll(userService.findAll().map { it -> "${it.name} (Role: ${User.roleToString(it.role)})" })
            prefHeight = 200.0
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
                    val userEditPane = showUserUpdateDialog(user)
                    userDetailsPane.children.setAll(userEditPane)
                }
            }
        }

        val addUserButton = Button("Add User").apply {
            setOnAction {
                showUserAddModal(userList)
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
                        helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.")
                    }
                } else {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Error", "No user selected to delete.")
                }
            }
        }

        val buttonBox = HBox(10.0, addUserButton, deleteUserButton)

        return VBox(10.0, Label("Manage Users"), userList, buttonBox, userDetailsPane).apply {
            style = "-fx-padding: 20px; -fx-background-color: #E8F5E9; -fx-spacing: 10px;"
        }
    }

    private fun showUserUpdateDialog(user: User): VBox {
        val nameField = TextField(user.name).apply {
            promptText = "Username"
        }

        val saveButton = Button("Save Changes").apply {
            setOnAction {
                val updatedName = nameField.text.trim()

                if (updatedName.isEmpty()) {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled out!")
                }  else {
                    val updatedUser = User(
                        id = user.id,
                        name = updatedName,
                        email = "",
                        password = "",
                        role = 2
                    )
                    userService.save(updatedUser)
                    helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Success", "User details updated successfully.")
                    onSettingsSaved()
                }
            }
        }

        return VBox(10.0, Label("Edit User"), nameField, saveButton).apply {
            style = "-fx-padding: 10px; -fx-border-color: #ccc; -fx-border-radius: 5px;"
        }
    }

    private fun showUserAddModal(userList: ListView<String>) {
        val dialog = Dialog<User>().apply {
            title = "Add New User"
            dialogPane.content = VBox(10.0).apply {
                val nameField = TextField().apply { promptText = "Username" }
                children.addAll(
                    Label("Add User Details"), nameField
                )
            }
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { buttonType ->
                if (buttonType == ButtonType.OK) {
                    val name = (dialogPane.content as VBox).children[1] as TextField

                    if (name.text.isNotEmpty()) {
                        User(
                            id = User.generateId(),
                            name = name.text,
                            email = " ",
                            password = " ",
                            role = 2
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
            userList.items.add("${user.name} (Role: ${User.roleToString(user.role)})")
        }
    }

}