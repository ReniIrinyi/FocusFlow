package view.admin

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.TimeLineSettings
import model.User
import controller.GenericController
import javafx.geometry.Insets
import javafx.scene.layout.GridPane
import utils.HelperFunctions

class TimelineSettings(
    userController: GenericController<User>,
    private val timelineSettingsController: GenericController<TimeLineSettings>,
    private val helperFunctions: HelperFunctions,
) {

    private val timelineCountDropdown = ComboBox<Int>()
    private val userDropdowns = List(3) { ComboBox<Pair<Int?, String>>() }
    private val users = userController.createRequest("GET",null,null,null,"all").first as List<User>

    /**
     * Erstellt die JavaFX-Ansicht zur Verwaltung der Timeline-Einstellungen im Admin-Bereich.
     *
     * @return Die erstellte Ansicht.
     */
    fun createView(): VBox {
        val availableUsers = users
        loadSettings(availableUsers)

        timelineCountDropdown.items.addAll(1, 2, 3)
        timelineCountDropdown.promptText = "Wählen Sie die Anzahl der Timelines"

        timelineCountDropdown.setOnAction {
            val count = timelineCountDropdown.value ?: 1
            userDropdowns.forEachIndexed { index, dropdown ->
                dropdown.isDisable = index >= count
                if (index >= count) dropdown.value = null to "Keine"
            }
        }

        val gridPane = GridPane().apply {
            hgap = 20.0
            vgap = 15.0
            padding = Insets(20.0)

            add(Label("Einstellungen der Zeitachse"), 0, 0, 2, 1) // Fejléc teljes szélességben

            add(Label("Anzahl der Timelines:"), 0, 1)
            add(timelineCountDropdown, 1, 1)
            timelineCountDropdown.styleClass.add("dropdown")

            userDropdowns.forEachIndexed { index, dropdown ->
                add(Label("Timeline ${index + 1} Benutzer:"), 0, index + 2)
                add(dropdown, 1, index + 2)
            }

            val saveButton = Button("Einstellungen speichern").apply {
                styleClass.add("custom-button")
                setOnAction {
                    saveSettings()
                    helperFunctions.showAlert(Alert.AlertType.INFORMATION, "Einstellungen gespeichert", "Timeline-Einstellungen wurden erfolgreich gespeichert.")
                }
            }

            userDropdowns.forEach { it.styleClass.add("dropdown")}

            add(saveButton, 0, userDropdowns.size + 2, 2, 1) // Mentés gomb szélesebb elhelyezés
        }

        return VBox(gridPane).apply {
            styleClass.add("grid-element")
            alignment = javafx.geometry.Pos.CENTER
        }
    }


    /**
     * Lädt bereits gespeicherte Einstellungen aus dem Service und initialisiert die Benutzer-Dropdowns.
     *
     * @param availableUsers Liste der verfügbaren Benutzer.
     */
    private fun loadSettings(availableUsers: List<User>) {
        val userOptions = listOf<Pair<Int?, String>>(null to "Keine") + availableUsers.map { it.id to it.name } // Benutzeroptionen für Dropdowns
        val allSettings = timelineSettingsController.createRequest("GET",null,null,null,"all").first as List<TimeLineSettings>

        val defaultCount = allSettings.firstOrNull()?.timeLineCount ?: 1
        timelineCountDropdown.value = defaultCount

        userDropdowns.forEachIndexed { index, dropdown ->
            dropdown.items.setAll(userOptions)

            val timelineSetting = allSettings.getOrNull(index)
            val userPair = userOptions.find { it.first == timelineSetting?.userId } ?: (null to "Keine") // Setzt zugewiesenen Benutzer oder "Keine"

            dropdown.value = userPair
            dropdown.isDisable = index >= defaultCount
        }
    }

    /**
     * Speichert die ausgewählten Einstellungen mithilfe des TimelineSettingsService.
     */
    private fun saveSettings() {
        val timelineCount = timelineCountDropdown.value ?: 1

        (0 until timelineCount).forEach { index ->
            val userId = userDropdowns[index].value?.first
            val timeLineSettings = TimeLineSettings(timelineCount, userId = userId ?: -1)
            timelineSettingsController.createRequest("PUT",null,null,timeLineSettings,"all")
        }
    }

}