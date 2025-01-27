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
    private val task: Task? = null,
    private val user: User
) {
    private var selectedFile: File? = null
    private val helperFunctions = HelperFunctions()

    fun createView(): VBox {
        val fileChooser = FileChooser().apply {
            title = "Bild auswählen"
            extensionFilters.add(FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"))
        }

        val startDatePicker = DatePicker(task?.startTime?.toLocalDate()).apply {
            styleClass.add("dropdown")
            promptText = "Startdatum auswählen"
            isEditable = false
        }

        val titleField = TextField(task?.title ?: "").apply {
            styleClass.add("input-text")
            prefWidth = 300.0
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
            styleClass.add("dropdown")
        }

        val startTimeSpinner = Spinner<Int>(0, 23, task?.startTime?.hour ?: 12).apply {
            styleClass.add("input")
            isEditable = true
        }
        val endTimeSpinner = Spinner<Int>(0, 23, task?.endTime?.hour ?: 13).apply {
            isEditable = true
            styleClass.add("input")
        }

        val selectedFileLabel = Label(task?.imageBase64?.let { "Bild ausgewählt" } ?: "Kein Bild ausgewählt").apply {
            styleClass.add("info-label")
        }

        val htmlEditor = HTMLEditor().apply {
            htmlText = task?.description ?: ""
            styleClass.add("html-editor-custom")
        }

        val customToolbar = HBox(10.0,
            titleField,
            startDatePicker,
            createDropdownButton("M12 2A10 10 0 1 0 22 12 10 10 0 0 0 12 2zm0 18A8 8 0 1 1 20 12 8 8 0 0 1 12 20zm-1-13h2v6h4v2h-6z", "Start-/Endzeit", startTimeSpinner, endTimeSpinner),
            priorityDropdown,
            createSVGIconButton("M5 8h14v10H5z M19 6H5V4h14v2z", "Bild hochladen") {
                val file = fileChooser.showOpenDialog(null)
                if (file != null) {
                    selectedFile = file
                    selectedFileLabel.text = "Bild: ${file.name}"
                }
            }
        ).apply {
            styleClass.add("toolbar-header")
        }

        val saveButton = Button("Speichern").apply {
            styleClass.add("custom-button")
            setOnAction {
                saveTask(titleField, priorityDropdown, startDatePicker,startTimeSpinner, endTimeSpinner, htmlEditor)
            }
        }
        val deleteButton = Button("Löschen").apply {
            styleClass.add("custom-button")
            setOnAction {
                deleteTask(titleField, priorityDropdown, startDatePicker,startTimeSpinner, endTimeSpinner, htmlEditor)
            }
        }

        val buttonContainer = HBox(10.0, saveButton, deleteButton).apply {
            padding = Insets(10.0,10.0,10.0,0.0)
        }

        return VBox(0.0,
            customToolbar,
            htmlEditor,
            buttonContainer
        )
    }

    private fun createSVGIconButton(svgContent: String, tooltipText: String, action: () -> Unit): Button {
        val svgIcon = SVGPath().apply {
            content = svgContent
            styleClass.add("svg-icon")
        }

        return Button(null, svgIcon).apply {
            tooltip = Tooltip(tooltipText)
            styleClass.add("svgButton")
            padding = Insets(10.0)
            setOnAction { action() }
        }
    }

    private fun saveTask(
        titleField: TextField,
        priorityDropdown: ComboBox<String>,
        startDatePicker: DatePicker,
        startTimePicker: Spinner<Int>,
        endTimePicker: Spinner<Int>,
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
            val endHour = endTimePicker.value
            val starHour = startTimePicker.value

            val startLocalTime = LocalTime.of(starHour, 0)
            val endLocalTime = LocalTime.of(endHour, 0)
            val startTime = LocalDateTime.of(startDate, startLocalTime)
            val endTime = LocalDateTime.of(startDate, endLocalTime)

            val updatedTask = Task(
                id = task?.id ?: Task.generateId(),
                userId = user.id,
                createdAt = task?.createdAt ?: LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                priority = priority,
                status = task?.status ?: 0,
                title = titleField.text,
                description = htmlEditor.htmlText,
                startTime = startTime,
                deadline = startTime,
                endTime = endTime,
                imageBase64 = base64Image
            )

            val requestType = if (task == null) "POST" else "PUT"
            taskController.createRequest(requestType, task?.id, null, updatedTask, null)
            helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Erfolg", "Aufgabe erfolgreich gespeichert!")
        } else {
            helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Bitte alle Felder ausfüllen!")
        }
    }

    private fun deleteTask(
        titleField: TextField,
        priorityDropdown: ComboBox<String>,
        startDatePicker: DatePicker,
        startTimeSpinner: Spinner<Int>,
        endTimeSpinner: Spinner<Int>,
        htmlEditor: HTMLEditor
    ) {
        if (task == null) {
            helperFunctions.showAlert(Alert.AlertType.ERROR, "Fehler", "Keine Aufgabe zum Löschen vorhanden!")
            return
        }

        val (response, status) = taskController.createRequest(
            requestTyp = "DELETE",
            task.id,
            userId = null,
            newData = null,
            routePath = null
        )

        if (status == 200) {
            titleField.clear()
            priorityDropdown.selectionModel.clearSelection()
            startDatePicker.value = null
            startTimeSpinner.valueFactory?.value = 12
            endTimeSpinner.valueFactory?.value = 13
            htmlEditor.htmlText = ""

            helperFunctions.showAlert(
                Alert.AlertType.INFORMATION,
                "Erfolg",
                "Aufgabe erfolgreich gelöscht!"
            )
        } else {
            helperFunctions.showAlert(
                Alert.AlertType.ERROR,
                "Fehler",
                "Konnte Aufgabe nicht löschen! (Status: $status)"
            )
        }
    }


    private fun createDropdownButton(svgContent: String, tooltipText: String, startSpinner: Spinner<Int>, endSpinner: Spinner<Int>): MenuButton {
        val svgIcon = SVGPath().apply {
            content = svgContent
            styleClass.add("svg-icon")
        }

        return MenuButton(null, svgIcon).apply {
            tooltip = Tooltip(tooltipText)
            items.addAll(
                MenuItem("Startzeit:").apply { contentDisplay = ContentDisplay.RIGHT; graphic = startSpinner },
                MenuItem("Endzeit:").apply { contentDisplay = ContentDisplay.RIGHT; graphic = endSpinner }
            )
            styleClass.add("svgButton")
        }
    }
}
