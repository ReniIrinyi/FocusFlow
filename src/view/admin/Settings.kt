package view.admin

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.FileChooser
import model.TimeLineSettings
import model.User
import controller.GenericController
import utils.Constants
import utils.HelperFunctions
import java.io.ByteArrayInputStream
import java.util.*

class Settings(
    private val userController: GenericController<User>,
    private val timeLineSettingsController: GenericController<TimeLineSettings>,
    private val helperFunctions: HelperFunctions,
    private val onSettingsSaved: () -> Unit
) {
    val users = userController.createRequest("GET", null, null, null, "all").first as List<User>

    fun createView(): VBox {
        val userListView = createUserListView()
        val authSettings = AdminAuthSettings(userController, helperFunctions) {}
        val timelineSettings = TimelineSettings(userController, timeLineSettingsController, helperFunctions)

        val gridPane = GridPane().apply {
            padding = Insets(20.0)
            hgap = 20.0
            vgap = 20.0
            styleClass.addAll("grid-container")

            columnConstraints.addAll(
                ColumnConstraints().apply {
                    hgrow = Priority.ALWAYS
                    percentWidth = 50.0
                },
                ColumnConstraints().apply {
                    hgrow = Priority.ALWAYS
                    percentWidth = 50.0
                }
            )

            rowConstraints.addAll(
                RowConstraints().apply {
                    vgrow = Priority.ALWAYS
                    percentHeight = 50.0
                }
            )

            // Benutzerverwaltung links oben
            add(userListView, 0, 0,1,2)

            // Auth-Einstellungen rechts oben
            add(authSettings.createView(), 1, 0)

            // Zeitleisten-Einstellungen rechts unten
            add(timelineSettings.createView(), 1, 1)
            VBox.setVgrow(this, Priority.ALWAYS)
        }


        return VBox(gridPane).apply {
            alignment = javafx.geometry.Pos.CENTER
            VBox.setVgrow(gridPane, Priority.ALWAYS)
            prefHeight = Double.MAX_VALUE
            prefWidth = Double.MAX_VALUE
        }

    }

    private fun createUserListView(): VBox {
        val userList = ListView<User>().apply {
            items.addAll(users)

            setCellFactory {
                object : ListCell<User>() {
                    private val imageView = ImageView().apply {
                        fitWidth = 50.0
                        fitHeight = 50.0
                        isPreserveRatio = true
                    }
                    private val nameLabel = Label()

                    override fun updateItem(user: User?, empty: Boolean) {
                        super.updateItem(user, empty)
                        if (empty || user == null) {
                            graphic = null
                        } else {
                            if (user.profileImage.isNotEmpty()) {
                                val imageBytes = Base64.getDecoder().decode(user.profileImage)
                                imageView.image = Image(ByteArrayInputStream(imageBytes))
                            } else {

                                imageView.image = Image(javaClass.getResourceAsStream("/images/default-avatar.png"))
                            }

                            nameLabel.text = user.name
                            nameLabel.style = "-fx-font-weight: bold;"

                            val hbox = HBox(10.0, imageView, nameLabel).apply {
                                padding = Insets(5.0)
                                alignment = javafx.geometry.Pos.CENTER_LEFT
                            }
                            graphic = hbox
                        }
                    }
                }
            }
        }

        userList.setOnMouseClicked {
            val selectedUser = userList.selectionModel.selectedItem
            if (selectedUser != null && selectedUser.role != 1) {  // Ha nem admin
                val dialog = TextInputDialog(selectedUser.name).apply {
                    title = "Benutzername ändern"
                    headerText = null
                    contentText = "Neuer Benutzername:"
                }
                val result = dialog.showAndWait()
                result.ifPresent { newName ->
                    if (newName.isNotEmpty()) {
                        selectedUser.name = newName
                        userController.createRequest("POST", null, null, selectedUser, null)
                        userList.refresh()
                    }
                }
            } else {
                helperFunctions.showAlert(Alert.AlertType.WARNING, "Hinweis", "Admin-Benutzername kann nicht geändert werden.")
            }
        }

        val addUserButton = Button("Benutzer hinzufügen").apply {
            styleClass.add("custom-button")
            setOnAction { showUserAddModal(userList) }
        }

        val deleteUserButton = Button("Benutzer löschen").apply {
            styleClass.add("custom-button")
            setOnAction {
                val selectedUser = userList.selectionModel.selectedItem
                if (selectedUser != null && selectedUser.role != 1) { // Admin törlésének tiltása
                    userController.createRequest("DELETE", null, selectedUser.id, null, null)
                    userList.items.remove(selectedUser)
                    helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Benutzer erfolgreich gelöscht.")
                } else {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Admin-Benutzer kann nicht gelöscht werden.")
                }
            }
        }

        val buttonBox = HBox(10.0, addUserButton, deleteUserButton)

        return VBox(10.0, Label("Benutzerverwaltung"), userList, buttonBox).apply {
            VBox.setVgrow(userList, Priority.ALWAYS)
            styleClass.add("grid-element")
        }
    }

    private fun showUserAddModal(userList: ListView<User>) {
        val dialog = Dialog<User>().apply {
            title = "Neuen Benutzer hinzufügen"

            var base64Image: String = ""
            val nameField = TextField().apply { promptText = "Benutzername" }
            val profileImageView = ImageView().apply {
                fitWidth = 100.0
                isPreserveRatio = true
            }

            val uploadButton = Button("Bild hochladen").apply {
                setOnAction {
                    val fileChooser = FileChooser().apply {
                        title = "Profilbild auswählen"
                        extensionFilters.addAll(FileChooser.ExtensionFilter("Bilddateien", "*.png", "*.jpg", "*.jpeg"))
                    }
                    val selectedFile = fileChooser.showOpenDialog(null)
                    if (selectedFile != null) {
                        val fileBytes = selectedFile.readBytes()
                        base64Image = Base64.getEncoder().encodeToString(fileBytes)
                        profileImageView.image = Image(ByteArrayInputStream(fileBytes))
                    }
                }
            }

            dialogPane.content = VBox(10.0, Label("Benutzer hinzufügen"), nameField, profileImageView, uploadButton).apply {
                prefHeight = 350.0
                prefWidth = 350.0
            }
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { if (it == ButtonType.OK) User(User.generateId(), nameField.text, "", "", 2, base64Image) else null }
        }

        val result = dialog.showAndWait()
        result.ifPresent { user ->
            val response = userController.createRequest("POST", null, null, user, null)
            if(response.second == Constants.RESTAPI_OK){
                userList.items.add(response.first as User)
            }
        }
    }
}
