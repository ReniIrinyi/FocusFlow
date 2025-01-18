package view.admin.AdminSettings

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.web.HTMLEditor
import javafx.stage.FileChooser
import model.Task
import service.TaskService
import service.UserService
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Diese Klasse verwaltet das Hinzufügen neuer Aufgaben.
 */
class TaskManager(private val taskService: TaskService, private val userService: UserService) {

    private var selectedFile: File? = null

    fun createView(): VBox {
        val fileChooser = FileChooser().apply {
            title = "Bild auswählen"
            extensionFilters.add(FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"))
        }

        val userDropdown = ComboBox<Pair<Int, String>>().apply {
            promptText = "Wähle einen Benutzer aus"
            items.addAll(userService.findAll().map { it.id to it.name })
        }

        val titleField = TextField().apply {
            promptText = "Aufgabentitel"
        }

        val priorityDropdown = ComboBox<String>().apply {
            promptText = "Priorität auswählen"
            items.addAll("Hoch", "Mittel", "Niedrig")
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

        val descriptionLabel = Label("Aufgabe Beschreibung:")
        val htmlEditor = HTMLEditor().apply {
            prefHeight = 300.0
        }

        val uploadButton = Button("Bild auswählen")
        val selectedFileLabel = Label("Kein Bild ausgewählt")

        uploadButton.setOnAction {
            val file = fileChooser.showOpenDialog(null)
            if (file != null) {
                selectedFile = file
                selectedFileLabel.text = "Ausgewähltes Bild: ${file.name}"
            }
        }

        val saveButton = Button("Aufgabe speichern").apply {
            setOnAction {
                saveTask(
                    taskService,
                    userDropdown,
                    titleField,
                    priorityDropdown,
                    startTimeField,
                    endTimeField,
                    deadlinePicker,
                    htmlEditor
                )
            }
        }

        return VBox(20.0,
            Label("Aufgabe erstellen"),
            userDropdown,
            titleField,
            priorityDropdown,
            timeFields,
            deadlinePicker,
            descriptionLabel,
            htmlEditor,
            uploadButton,
            selectedFileLabel,
            saveButton
        ).apply {
            spacing = 20.0
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }
    }

    private fun saveTask(
        taskService: TaskService,
        userDropdown: ComboBox<Pair<Int, String>>,
        titleField: TextField,
        priorityDropdown: ComboBox<String>,
        startTimeField: Spinner<Int>,
        endTimeField: Spinner<Int>,
        deadlinePicker: DatePicker,
        htmlEditor: HTMLEditor
    ) {
        if (titleField.text.isNotEmpty() &&
            priorityDropdown.value != null &&
            userDropdown.value != null
        ) {
            val base64Image = selectedFile?.absolutePath?.let { taskService.encodeImageToBase64(it) } ?: ""
            val priority = when (priorityDropdown.value) {
                "Hoch" -> 1
                "Mittel" -> 2
                "Niedrig" -> 3
                else -> throw IllegalArgumentException("Ungültige Priorität")
            }
            val startHour = startTimeField.value
            val endHour = endTimeField.value
            val today = LocalDate.now()
            val startLocalTime = LocalTime.of(startHour, 0)
            val endLocalTime = LocalTime.of(endHour, 0)
            val startTime = LocalDateTime.of(today, startLocalTime)
            val endTime = LocalDateTime.of(today, endLocalTime)
            val deadline = deadlinePicker.value?.atStartOfDay()
            val selectedUser = userDropdown.value.first
            val description = htmlEditor.htmlText

            val newTask = Task(
                id = Task.generateId(),
                userId = selectedUser,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                priority = priority,
                status = 0,
                title = titleField.text,
                description = description, // HTML
                startTime = startTime,
                deadline = deadline,
                endTime = endTime,
                imageBase64 = base64Image
            )

            taskService.save(newTask)
            println("Aufgabe gespeichert: ${newTask.title} mit Beschreibung: ${description.take(30)}…")
        } else {
            println("Bitte alle Felder ausfüllen und eine Priorität setzen!")
        }
    }
}
