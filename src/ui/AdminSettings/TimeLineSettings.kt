package ui.AdminSettings

import UserService
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.io.File

data class User(val id: Int, val name: String)

class TimelineSettings(private val userService: UserService) {

    private val settingsFile = File("timeline.txt")
    private val timelineCountDropdown = ComboBox<Int>()
    private val userDropdowns = List(3) { ComboBox<Pair<Int?, String>>() }

    fun createView(): VBox {
        val availableUsers = userService.getUsers().map { User(it.id, it.name) }
        println(availableUsers)
        loadSettings(availableUsers)

        // Timeline count dropdown
        timelineCountDropdown.items.addAll(1, 2, 3)
        timelineCountDropdown.promptText = "Select Number of Timelines"

        // Enable user dropdowns based on selected timeline count
        timelineCountDropdown.setOnAction {
            val count = timelineCountDropdown.value ?: 1
            userDropdowns.forEachIndexed { index, dropdown ->
                dropdown.isDisable = index >= count
                if (index >= count) dropdown.value = null to "None"
            }
        }

        val saveButton = Button("Save Settings").apply {
            setOnAction {
                saveSettings()
                showAlert(Alert.AlertType.INFORMATION, "Settings Saved", "Timeline settings saved successfully.")
            }
        }

        val settingsBox = VBox(10.0).apply {
            children.addAll(
                Label("Configure Timeline Settings"),
                HBox(10.0, Label("Number of Timelines:"), timelineCountDropdown),
                *userDropdowns.mapIndexed { index, dropdown ->
                    HBox(10.0, Label("Timeline ${index + 1} User:"), dropdown)
                }.toTypedArray(),
                saveButton
            )
            style = "-fx-padding: 20px; -fx-background-color: #FFF3E0;"
        }

        return settingsBox
    }

    private fun saveSettings() {
        val timelineCount = timelineCountDropdown.value ?: 1
        val selectedUsers = userDropdowns.map { it.value?.first?.toString() ?: "None" }

        val content = "$timelineCount|${selectedUsers.joinToString("|")}"
        settingsFile.writeText(content)
    }

    private fun loadSettings(availableUsers: List<User>) {
        if (settingsFile.exists()) {
            val content = settingsFile.readText().trim()
            val parts = content.split("|")
            if (parts.isNotEmpty()) {
                val timelineCount = parts[0].toIntOrNull() ?: 1
                timelineCountDropdown.value = timelineCount
                val userOptions = listOf<Pair<Int?, String>>(null to "None") + availableUsers.map { it.id to it.name }
                parts.drop(1).forEachIndexed { index, userIdOrNone ->
                    if (index < userDropdowns.size) {
                        val userPair = userOptions.find { it.first?.toString() == userIdOrNone }
                            ?: (null to "None")

                        userDropdowns[index].items.setAll(userOptions)
                        userDropdowns[index].value = userPair
                        userDropdowns[index].isDisable = index >= timelineCount

                    }
                }
            }
        }
    }


    private fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title
            this.contentText = message
            showAndWait()
        }
    }
}
