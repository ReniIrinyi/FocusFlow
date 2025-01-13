package ui.add

import UserService
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import model.Task
import service.TaskService
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Diese Klasse verwaltet das Hinzufügen neuer Aufgaben.
 */
class AddMenu {

    fun createView(taskService: TaskService, userService: UserService): VBox {
        val fileChooser = FileChooser().apply {
            title = "Bild auswählen"
            extensionFilters.add(FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"))
        }

        val userDropdown = ComboBox<Pair<Int, String>>().apply {
            promptText = "Wähle einen Benutzer aus"
            items.addAll(userService.getUsers().map { it.id to it.name })
        }

        val titleField = TextField().apply {
            promptText = "Aufgabentitel"
        }

        val priorityField = TextField().apply {
            promptText = "Priorität (Hoch, Mittel, Niedrig)"
        }

        val startTimeField = Spinner<Int>(0, 23, 12).apply {
            isEditable = true
        }

        val endTimeField = Spinner<Int>(0, 23, 13).apply {
            isEditable = true
        }

        val timeFields = HBox(10.0,
            Label("Startzeit:"), startTimeField,
            Label("Endzeit:"), endTimeField
        )

        val deadlinePicker = DatePicker().apply {
            promptText = "Deadline auswählen"
        }

        val uploadButton = Button("Bild auswählen")
        val selectedFileLabel = Label("Kein Bild ausgewählt")

        var selectedFile: File? = null

        uploadButton.setOnAction {
            val file = fileChooser.showOpenDialog(null)
            if (file != null) {
                selectedFile = file
                selectedFileLabel.text = "Ausgewähltes Bild: ${file.name}"
            }
        }

        val saveButton = Button("Aufgabe speichern").apply {
            setOnAction {
                if (titleField.text.isNotEmpty() &&
                    priorityField.text.isNotEmpty() &&
                    selectedFile != null
                ) {
                    val base64Image = taskService.encodeImageToBase64(selectedFile!!.absolutePath)
                    val priority = priorityField.text
                    val startHour = startTimeField.value
                    val endHour = endTimeField.value
                    val today = LocalDate.now()
                    val startLocalTime = LocalTime.of(startHour, 0)
                    val endLocalTime = LocalTime.of(endHour, 0)
                    val startTime = LocalDateTime.of(today, startLocalTime)
                    val endTime = LocalDateTime.of(today, endLocalTime)
                    val deadline = deadlinePicker.value?.atStartOfDay()
                    val selectedUser = userDropdown.value.first

                    val newTask = Task(
                        id = Task.generateId(),
                        title = titleField.text,
                        priority = priority,
                        createdAt = LocalDateTime.now(),
                        updatedAt = LocalDateTime.now(),
                        startTime = startTime,
                        endTime = endTime,
                        deadline = deadline,
                        status = "Nicht erledigt",
                        imageBase64 = base64Image,
                        userId = selectedUser,
                    )

                    taskService.add(newTask)
                    println("Aufgabe gespeichert: ${newTask.title} für user: ${selectedUser}")
                } else {
                    println("Bitte alle Felder ausfüllen und ein Bild auswählen!")
                }
            }
        }

        return VBox(20.0,
            Label("Aufgabe erstellen"),
            userDropdown,
            titleField,
            priorityField,
            timeFields,
            deadlinePicker,
            uploadButton,
            selectedFileLabel,
            saveButton
        ).apply {
            spacing = 20.0
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }
    }
}
