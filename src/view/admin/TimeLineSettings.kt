package view.admin

import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.TimeLineSettings
import model.User
import controller.GenericController
import utils.HelperFunctions

class TimelineSettings(
    private val userController: GenericController<User>,
    private val timelineSettingsController: GenericController<TimeLineSettings>,
    private val helperFunctions: HelperFunctions,
) {

    private val timelineCountDropdown = ComboBox<Int>() // Dropdown-Menü für die Auswahl der Timeline-Anzahl
    private val userDropdowns = List(3) { ComboBox<Pair<Int?, String>>() } // Drei Dropdowns für die Benutzerzuweisung zu den Timelines
    val users = userController.createRequest("GET",null,null,null,"all").first as List<User>

    /**
     * Erstellt die JavaFX-Ansicht zur Verwaltung der Timeline-Einstellungen im Admin-Bereich.
     *
     * @return Die erstellte Ansicht.
     */
    fun createView(): VBox {
        val availableUsers = users
        println(availableUsers)
        loadSettings(availableUsers) // Einstellungen laden und Dropdowns initialisieren

        // Dropdown für die Auswahl der Timeline-Anzahl
        timelineCountDropdown.items.addAll(1, 2, 3) // Timeline-Anzahl 1 bis 3
        timelineCountDropdown.promptText = "Wählen Sie die Anzahl der Timelines"

        // Aktiviert oder deaktiviert Dropdowns basierend auf der gewählten Anzahl von Timelines
        timelineCountDropdown.setOnAction {
            val count = timelineCountDropdown.value ?: 1 // Standardmäßig mindestens 1
            userDropdowns.forEachIndexed { index, dropdown ->
                dropdown.isDisable = index >= count // Dropdown deaktivieren, wenn über die gewählte Zahl hinaus
                if (index >= count) dropdown.value = null to "Keine" // Kein Benutzer zuweisen, wenn deaktiviert
            }
        }

        val saveButton = Button("Einstellungen speichern").apply {
            setOnAction {
                saveSettings() // Einstellungen speichern
                helperFunctions.showAlert( (Alert.AlertType.INFORMATION), "Einstellungen gespeichert", "Timeline-Einstellungen wurden erfolgreich gespeichert.")
            }
        }

        // Layout für die Anzeige der Konfiguration
        val settingsBox = VBox(10.0).apply {
            children.addAll(
                Label("Einstellungen der Zeitachse"),
                HBox(10.0, Label("Anzahl der Timelines:"), timelineCountDropdown),
                *userDropdowns.mapIndexed { index, dropdown -> // Dropdowns für die Benutzerzuweisungen
                    HBox(10.0, Label("Timeline ${index + 1} Benutzer:"), dropdown)
                }.toTypedArray(),
                saveButton
            )
        }

        return settingsBox
    }

    /**
     * Lädt bereits gespeicherte Einstellungen aus dem Service und initialisiert die Benutzer-Dropdowns.
     *
     * @param availableUsers Liste der verfügbaren Benutzer.
     */
    private fun loadSettings(availableUsers: List<User>) {
        val userOptions = listOf<Pair<Int?, String>>(null to "Keine") + availableUsers.map { it.id to it.name } // Benutzeroptionen für Dropdowns
        val allSettings = timelineSettingsController.createRequest("GET",null,null,null,"all").first as List<TimeLineSettings>

        // Standardmäßige Timeline-Anzahl einstellen
        val defaultCount = allSettings.firstOrNull()?.timeLineCount ?: 1 // Falls keine existieren, Standardwert 1
        timelineCountDropdown.value = defaultCount // Setzt den Standardwert im Dropdown

        // Initialisiert die Benutzerzuweisungen basierend auf den gespeicherten Einstellungen
        userDropdowns.forEachIndexed { index, dropdown ->
            dropdown.items.setAll(userOptions) // Erlaubte Optionen setzen

            val timelineSetting = allSettings.getOrNull(index) // Nimmt die gespeicherten Werte, falls vorhanden
            val userPair = userOptions.find { it.first == timelineSetting?.userId } ?: (null to "Keine") // Setzt zugewiesenen Benutzer oder "Keine"

            dropdown.value = userPair // Setzt den Standardwert im Dropdown
            dropdown.isDisable = index >= defaultCount // Deaktiviert Dropdowns, die über die Timeline-Anzahl hinausgehen
        }
    }

    /**
     * Speichert die ausgewählten Einstellungen mithilfe des TimelineSettingsService.
     */
    private fun saveSettings() {
        val timelineCount = timelineCountDropdown.value ?: 1 // Anzahl der aktiven Timelines

        (0 until timelineCount).forEach { index ->
            val userId = userDropdowns[index].value?.first // Holt Benutzer-ID aus dem Dropdown
            val timeLineSettings = TimeLineSettings(timelineCount, userId = userId ?: -1) // Erstellt ein TimeLineSettings-Objekt
            timelineSettingsController.createRequest("PUT",null,null,timeLineSettings,"all")
        }
    }

}