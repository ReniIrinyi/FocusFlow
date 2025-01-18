package view.admin.AdminSettings
import javafx.scene.control.*
import javafx.scene.layout.HBox

class Header(
    private val onSectionSelected: (String) -> Unit
) {

    fun createHeader(): HBox {
        val userSettingsButton = Button("User Settings").apply {
            setOnAction { onSectionSelected("UserSettings") }
        }

        val timelineSettingsButton = Button("Timeline Settings").apply {
            setOnAction { onSectionSelected("TimelineSettings") }
        }

        val otherSettingsButton = Button("Other Settings").apply {
            setOnAction { onSectionSelected("OtherSettings") }
        }

        return HBox(10.0, userSettingsButton, timelineSettingsButton, otherSettingsButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #ECECEC; -fx-spacing: 15px;"
        }
    }
}