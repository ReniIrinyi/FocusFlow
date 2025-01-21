package view.admin.AdminSettings

import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import model.User
import service.UserService
import utils.HelperFunctions
import java.io.ByteArrayInputStream
import java.util.*

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

        val profileImageView = ImageView().apply {
            // Ha már van a user-nek mentett képe (Base64), azt megjeleníthetjük:
            if (user.profileImage.isNotBlank()) {
                val bytes = Base64.getDecoder().decode(user.profileImage)
                image = Image(ByteArrayInputStream(bytes))
            }
            fitWidth = 100.0
            isPreserveRatio = true
        }

        // Gomb a kép feltöltéséhez
        val uploadButton = Button("Upload Image").apply {
            setOnAction {
                val fileChooser = javafx.stage.FileChooser().apply {
                    title = "Select Profile Image"
                    extensionFilters.addAll(
                        FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                    )
                }

                val selectedFile = fileChooser.showOpenDialog(null)
                if (selectedFile != null) {
                    val fileBytes = selectedFile.readBytes()
                    val base64String = Base64.getEncoder().encodeToString(fileBytes)
                    user.profileImage = base64String
                    profileImageView.image = Image(ByteArrayInputStream(fileBytes))
                }
            }
        }

        val saveButton = Button("Save Changes").apply {
            setOnAction {
                val updatedName = nameField.text.trim()

                if (updatedName.isEmpty()) {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Error", "All fields must be filled out!")
                } else {
                    user.name = updatedName
                    userService.save(user)
                    helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Success", "User details updated successfully.")
                    onSettingsSaved()
                }
            }
        }

        return VBox(10.0).apply {
            children.addAll(
                Label("Edit User"),
                nameField,
                profileImageView,
                uploadButton,
                saveButton
            )
            style = "-fx-padding: 10px; -fx-border-color: #ccc; -fx-border-radius: 5px;"
        }
    }


    private fun showUserAddModal(userList: ListView<String>) {
        val dialog = Dialog<User>().apply {
            title = "Add New User"

            var base64Image: String = ""
            val nameField = TextField().apply { promptText = "Username" }
            val profileImageView = ImageView().apply {
                fitWidth = 100.0
                isPreserveRatio = true
            }

            val uploadButton = Button("Upload Image").apply {
                setOnAction {
                    val fileChooser = javafx.stage.FileChooser().apply {
                        title = "Select Profile Image"
                        extensionFilters.addAll(
                            FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
                        )
                    }
                    val selectedFile = fileChooser.showOpenDialog(null)
                    if (selectedFile != null) {
                        val fileBytes = selectedFile.readBytes()
                        base64Image = Base64.getEncoder().encodeToString(fileBytes)
                        profileImageView.image = Image(ByteArrayInputStream(fileBytes))
                    }
                }
            }

            dialogPane.content = VBox(10.0).apply {
                children.addAll(
                    Label("Add User Details"),
                    nameField,
                    profileImageView,
                    uploadButton
                )
            }

            // OK/Cancel gombok
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { buttonType ->
                if (buttonType == ButtonType.OK) {
                    val name = nameField.text.trim()
                    if (name.isNotEmpty()) {
                        User(
                            id = User.generateId(),
                            name = name,
                            email = " ",
                            password = " ",
                            role = 2,
                            profileImage = base64Image
                        )
                    } else {
                        null
                    }
                } else {
                    null
                }
            }
        }

        val result = dialog.showAndWait()
        result.ifPresent { user ->
            userService.save(user)
            userList.items.add("${user.name} (Role: ${User.roleToString(user.role)})")
        }
    }


}