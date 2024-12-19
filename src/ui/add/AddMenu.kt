package ui.add

import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import javafx.stage.Stage
import model.Task
import service.TaskService
import java.io.File
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

/**
 * Diese Klasse verwaltet das Hinzufügen neuer Aufgaben.
 *    - Die Geschäftslogik wird durch den TaskService bereitgestellt.
 *
 * Zeigt das Menü zum Hinzufügen neuer Aufgaben an.
 * TODO @alex:
 * 1. Fordere den Benutzer auf, die folgenden Informationen einzugeben:
 *    - Titel der Aufgabe.
 *    - Priorität der Aufgabe (Hoch, Mittel, Niedrig).
 *    - Fälligkeitsdatum der Aufgabe (YYYY-MM-DD).
 * 2. Stelle sicher:
 *    - Keine Eingabe ist leer.
 *    - Die Priorität ist gültig (verwende `InputValidator.isValidPriority`).
 *    - Das Fälligkeitsdatum ist ein gültiges Datum (verwende `InputValidator.isValidDate`).
 * 3. Erstelle ein neues `Task`-Objekt mit den eingegebenen Informationen.
 * 4. Verwende `taskService.addTask()`, um die Aufgabe zur Liste hinzuzufügen.
 * 5. Gib dem Benutzer eine Bestätigung aus, dass die Aufgabe erfolgreich hinzugefügt wurde.
 */
class AddMenu() {

     fun openUploadMenu(taskService: TaskService) {
        val stage = Stage()
        stage.title = "Aufgabe mit Bild hochladen"

        val fileChooser = FileChooser().apply {
            title = "Bild auswählen"
            extensionFilters.add(FileChooser.ExtensionFilter("Bilder", "*.png", "*.jpg", "*.jpeg"))
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

         val timeFields = HBox(10.0, Label("Startzeit:"), startTimeField, Label("Endzeit:"), endTimeField)

         val deadlinePicker = DatePicker().apply {
             promptText = "Deadline"
         }

        val uploadButton = Button("Bild auswählen")
        val selectedFileLabel = Label("Kein Bild ausgewählt")

        var selectedFile: File? = null

        uploadButton.setOnAction {
            val file = fileChooser.showOpenDialog(stage)
            if (file != null) {
                selectedFile = file
                selectedFileLabel.text = "Ausgewähltes Bild: ${file.name}"
            }
        }

         val saveButton = Button("Aufgabe speichern").apply {
             setOnAction {
                 if (selectedFile != null && titleField.text.isNotEmpty()) {
                     val base64Image = taskService.encodeImageToBase64(selectedFile!!.absolutePath)

                     val priority = priorityField.text.ifEmpty { "Keine Priorität" }
                     val startTime = LocalTime.of(startTimeField.value, 0)
                     val endTime = LocalTime.of(endTimeField.value, 0)
                     val deadline = deadlinePicker.value?.atStartOfDay()

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
                         imageBase64 = base64Image
                     )

                     taskService.add(newTask)
                     println("Aufgabe gespeichert: ${newTask.title}")
                     stage.close()
                 } else {
                     println("Bitte alle Felder ausfüllen und ein Bild auswählen!")
                 }
             }
         }

         val layout = VBox(20.0, Label("Aufgabe erstellen"), titleField, priorityField, timeFields, deadlinePicker, uploadButton, selectedFileLabel, saveButton).apply {
             spacing = 20.0
         }
        layout.spacing = 20.0

        val scene = Scene(layout, 400.0, 500.0)
        stage.scene = scene
        stage.show()
    }
}
