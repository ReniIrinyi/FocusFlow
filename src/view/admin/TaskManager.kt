package view.admin

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.shape.SVGPath
import javafx.scene.web.HTMLEditor
import javafx.stage.FileChooser
import model.Task
import model.User
import controller.GenericController
import utils.HelperFunctions
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TaskManager(
    private val taskController: GenericController<Task>,
    private val userController: GenericController<User>,
    private val task: Task? = null,
    private val user: User
) {
    private var selectedFile: File? = null
    private val users = userController.createRequest("GET", null, null, null, "all").first as List<User>
    private val helperFunctions = HelperFunctions()

    fun createView(): VBox {
        println("createView... ")

        val fileChooser = FileChooser().apply {
            title = "Bild auswählen"
            extensionFilters.add(FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"))
        }

        // Startdatum (ehem. Deadline)
        val startDatePicker = DatePicker(task?.startTime?.toLocalDate()).apply {
            promptText = "Startdatum auswählen"
            isEditable = false
        }

        val titleField = TextField(task?.title ?: "").apply {
            promptText = "Titel der Aufgabe"
        }

        val priorityDropdown = ComboBox<String>().apply {
            items.addAll("Hoch", "Mittel", "Niedrig")
            promptText = "Priorität"
            maxWidth = 120.0
            selectionModel.select(
                when (task?.priority) {
                    1 -> "Hoch"
                    2 -> "Mittel"
                    3 -> "Niedrig"
                    else -> null
                }
            )
        }

        val startTimeSpinner = Spinner<Int>(0, 23, task?.startTime?.hour ?: 12).apply { isEditable = true }
        val endTimeSpinner = Spinner<Int>(0, 23, task?.endTime?.hour ?: 13).apply { isEditable = true }

        val selectedFileLabel = Label(task?.imageBase64?.let { "Bild ausgewählt" } ?: "Kein Bild ausgewählt").apply {
            style = "-fx-font-size: 12px; -fx-text-fill: #555;"
        }

        val htmlEditor = HTMLEditor().apply {
            htmlText = task?.description ?: ""
        }

        val toolbar = HBox(0.0).apply {

                val createdAt = task?.createdAt?.toLocalDate() ?: LocalDate.now()
                val updatedAt = task?.updatedAt?.toLocalDate()
                val createdAtLabel = Label("Erstellt am: ${createdAt}").apply {
                    style = "-fx-font-size: 14px;-fx-padding: 0 0 0 12px; -fx-text-fill: #777777; -fx-alignment: center-right;"
                    maxWidth = Double.MAX_VALUE
                }


            children.addAll(
                startDatePicker,  // Startdatum Feld
                createDropdownButton("M12 2A10 10 0 1 0 22 12 10 10 0 0 0 12 2zm0 18A8 8 0 1 1 20 12 8 8 0 0 1 12 20zm-1-13h2v6h4v2h-6z", "Start-/Endzeit", startTimeSpinner, endTimeSpinner),
                priorityDropdown,
                createSVGIconButton("M5 8h14v10H5z M19 6H5V4h14v2z", "Bild hochladen") {
                    val file = fileChooser.showOpenDialog(null)
                    if (file != null) {
                        selectedFile = file
                        selectedFileLabel.text = "Bild: ${file.name}"
                    }
                },
                titleField,
                createdAtLabel
            )
            padding = Insets(10.0)
            style = "-fx-background-color: #E0E0E0; -fx-border-radius: 5px; -fx-border-color: #CCC;"
        }

        val saveButton = Button("Speichern").apply {
            setOnAction {
                saveTask(titleField, priorityDropdown, startTimeSpinner, endTimeSpinner, startDatePicker, htmlEditor)
            }
        }

        return VBox(10.0,
            toolbar,
            selectedFileLabel,
            htmlEditor,
            saveButton
        ).apply {
            spacing = 10.0
            style = "-fx-padding: 20px; -fx-background-color: #F5F5F5;"
        }
    }



    private fun createSVGIconButton(svgContent: String, tooltipText: String, action: () -> Unit): Button {
        val svgIcon = SVGPath().apply {
            content = svgContent
            style = "-fx-fill: #4A4A4A; -fx-scale-x: 1.2; -fx-scale-y: 1.2;"
        }

        return Button(null, svgIcon).apply {
            tooltip = Tooltip(tooltipText)
            style = "-fx-background-color: transparent; -fx-border: none; -fx-cursor: hand;"
            setOnAction { action() }
        }
    }

    private fun saveTask(
        titleField: TextField,
        priorityDropdown: ComboBox<String>,
        startTimePicker: Spinner<Int>,
        endTimePicker: Spinner<Int>,
        startDatePicker: DatePicker,
        htmlEditor: HTMLEditor
    ) {
        if (titleField.text.isNotEmpty() && priorityDropdown.value != null) {
            val base64Image = selectedFile?.absolutePath?.let { helperFunctions.encodeImageToBase64(it) } ?: task?.imageBase64 ?: ""
            val priority = when (priorityDropdown.value) {
                "Hoch" -> 1
                "Mittel" -> 2
                "Niedrig" -> 3
                else -> throw IllegalArgumentException("Ungültige Priorität")
            }
            val startDate = startDatePicker.value
            val startHour = startTimePicker.value
            val endHour = endTimePicker.value

            val startLocalTime = LocalTime.of(startHour, 0)
            val endLocalTime = LocalTime.of(endHour, 0)
            val startTime = LocalDateTime.of(startDate, startLocalTime)
            val endTime = LocalDateTime.of(startDate, endLocalTime)
            val starDate = LocalDateTime.of(startDate,startLocalTime)
            val selectedUser = user
            val description = htmlEditor.htmlText

            val updatedTask = Task(
                id = task?.id ?: Task.generateId(),
                userId = selectedUser.id,
                createdAt = task?.createdAt ?: LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                priority = priority,
                status = task?.status ?: 0,
                title = titleField.text,
                description = description,
                startTime = startTime,
                deadline = starDate,
                endTime = endTime,
                imageBase64 = base64Image
            )

            val requestType = if (task == null) "POST" else "PUT"
            taskController.createRequest(requestType, task?.id, null, updatedTask, null)
            println("Aufgabe gespeichert: ${updatedTask.title} mit Beschreibung: ${description.take(30)}…")
        } else {
            println("Bitte alle Felder ausfüllen und eine Priorität setzen!")
        }
    }



    private fun createDropdownButton(svgContent: String, tooltipText: String, startSpinner: Spinner<Int>, endSpinner: Spinner<Int>): MenuButton {
        val svgIcon = SVGPath().apply {
            content = svgContent
            style = "-fx-fill: #4A4A4A; -fx-scale-x: 1.2; -fx-scale-y: 1.2;"
        }

        return MenuButton(null, svgIcon).apply {
            tooltip = Tooltip(tooltipText)
            items.addAll(
                MenuItem("Startzeit:").apply { contentDisplay = ContentDisplay.RIGHT; graphic = startSpinner },
                MenuItem("Endzeit:").apply { contentDisplay = ContentDisplay.RIGHT; graphic = endSpinner }
            )
            style = "-fx-background-color: transparent; -fx-border: none;"
        }
    }
}
