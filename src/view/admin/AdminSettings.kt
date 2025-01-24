package view.admin

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.stage.FileChooser
import model.Task
import model.TimeLineSettings
import model.User
import controller.GenericController
import utils.HelperFunctions
import java.io.ByteArrayInputStream
import java.util.*

class AdminSettings(
    private val taskController: GenericController<Task>,
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

        // Layout mit GridPane für bessere Struktur
        val gridPane = GridPane().apply {
            padding = Insets(20.0)
            hgap = 20.0
            vgap = 20.0
            style = " -fx-background-color: #F5F5F5;"

            columnConstraints.addAll(
                ColumnConstraints().apply {
                    hgrow = Priority.ALWAYS  // Nimmt den gesamten verfügbaren Platz ein
                    percentWidth = 33.0
                },
                ColumnConstraints().apply {
                    hgrow = Priority.ALWAYS
                    percentWidth = 100.0       // 67% für den rechten Bereich (Auth + Timeline)
                }
            )

            rowConstraints.addAll(
                RowConstraints().apply {
                    vgrow = Priority.ALWAYS
                    percentHeight = 50.0       // 50% für die obere Zeile (Auth)
                },
                RowConstraints().apply {
                    vgrow = Priority.ALWAYS
                    percentHeight = 50.0       // 50% für die untere Zeile (Timeline)
                }
            )

            // Benutzerverwaltung links oben
            add(userListView, 0, 0)

            // Benutzer-Detailbereich links unten
            add(userDetailsPane, 0, 1)

            // Auth-Einstellungen rechts oben
            add(authSettings.createView(), 1, 0)

            // Zeitleisten-Einstellungen rechts unten
            add(timelineSettings.createView(), 1, 1)
        }

        return VBox(gridPane).apply {
            alignment = javafx.geometry.Pos.CENTER
        }
    }

    private lateinit var userDetailsPane: VBox

    private fun createUserListView(): VBox {
        val userList = ListView<String>().apply {
            items.addAll(users.map { "${it.name} (Role: ${User.roleToString(it.role)})" })
        }

        userDetailsPane = VBox().apply {
            children.add(Label("Bitte einen Benutzer auswählen."))
        }

        userList.setOnMouseClicked {
            val selectedUser = userList.selectionModel.selectedItem
            if (selectedUser != null) {
                val userName = selectedUser.substringBefore(" (")
                val user = users.find { it.name == userName }
                if (user != null) {
                    val userEditPane = showUserUpdateDialog(user)
                    userDetailsPane.children.setAll(userEditPane)
                }
            }
        }

        val addUserButton = Button("Benutzer hinzufügen").apply {
            setOnAction { showUserAddModal(userList) }
        }

        val deleteUserButton = Button("Benutzer löschen").apply {
            setOnAction {
                val selectedUser = userList.selectionModel.selectedItem
                if (selectedUser != null) {
                    val userName = selectedUser.substringBefore(" (")
                    val user = users.find { it.name == userName }
                    if (user != null) {
                        userController.createRequest("DELETE", null, user.id, null, null)
                        userList.items.remove(selectedUser)
                        userDetailsPane.children.setAll(Label("Bitte einen Benutzer auswählen."))
                        helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Benutzer erfolgreich gelöscht.")
                    }
                } else {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Kein Benutzer zum Löschen ausgewählt.")
                }
            }
        }

        val buttonBox = HBox(10.0, addUserButton, deleteUserButton)

        return VBox(10.0, Label("Benutzerverwaltung"), userList, buttonBox).apply {
        }
    }

    private fun showUserUpdateDialog(user: User): VBox {
        val nameField = TextField(user.name).apply {
            promptText = "Benutzername"
        }

        val profileImageView = ImageView().apply {
            if (user.profileImage.isNotBlank()) {
                val bytes = Base64.getDecoder().decode(user.profileImage)
                image = Image(ByteArrayInputStream(bytes))
            }
            fitWidth = 100.0
            isPreserveRatio = true
        }

        val uploadButton = Button("Bild hochladen").apply {
            setOnAction {
                val fileChooser = FileChooser().apply {
                    title = "Profilbild auswählen"
                    extensionFilters.addAll(
                        FileChooser.ExtensionFilter("Bilddateien", "*.png", "*.jpg", "*.jpeg")
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

        val saveButton = Button("Änderungen speichern").apply {
            setOnAction {
                val updatedName = nameField.text.trim()

                if (updatedName.isEmpty()) {
                    helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Alle Felder müssen ausgefüllt werden!")
                } else {
                    user.name = updatedName
                    userController.createRequest("POST", null, null, user, null)
                    helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Benutzerdaten erfolgreich aktualisiert.")
                    onSettingsSaved()
                }
            }
        }

        return VBox(10.0, Label("Benutzer bearbeiten"), nameField, profileImageView, uploadButton, saveButton).apply {
            style = "-fx-padding: 10px; -fx-border-color: #ccc;"
        }
    }

    private fun showUserAddModal(userList: ListView<String>) {
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

            dialogPane.content = VBox(10.0, Label("Benutzer hinzufügen"), nameField, profileImageView, uploadButton)
            dialogPane.buttonTypes.addAll(ButtonType.OK, ButtonType.CANCEL)

            setResultConverter { if (it == ButtonType.OK) User(User.generateId(), nameField.text, "", "", 2, base64Image) else null }
        }

        val result = dialog.showAndWait()
        result.ifPresent { user ->
            userController.createRequest("POST", null, null, user, null)
            userList.items.add("${user.name} (Role: ${User.roleToString(user.role)})")
        }
    }
}
