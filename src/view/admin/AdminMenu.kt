package view.admin

import javafx.scene.control.Button
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import model.Task
import model.TimeLineSettings
import model.User
import controller.GenericController
import utils.HelperFunctions

class AdminMenu(
    private val taskController: GenericController<Task>,
    private val userController: GenericController<User>,
    private val timeLineSettingsController: GenericController<TimeLineSettings>, private val helperFunctions: HelperFunctions,) {

    private val content = StackPane()

    init {
        val taskOverview = TaskOverview(taskController, userController)
        content.children.setAll(taskOverview.createView())
    }

    fun createView(): VBox {
        val header = createAdminSettingsHeader()
        return VBox( header, HBox( content)).apply {
            styleClass.add("adminMenu-root")
        }
    }

    private fun createAdminSettingsHeader(): HBox {
        val userSettingsButton = Button("Einstellungen Admin").apply {
            setOnAction {
                val adminSettings = AdminSettings(taskController,userController,timeLineSettingsController, helperFunctions) {
                    refreshView()
                }
                content.children.setAll(adminSettings.createView())
            }
        }

        val taskOverviewButton = Button("Aufgabenübersicht").apply {
            setOnAction {
                val taskOverview = TaskOverview(taskController, userController)
                content.children.setAll(taskOverview.createView())
            }
        }

        return HBox(10.0,taskOverviewButton, userSettingsButton).apply {
            style = "-fx-padding: 10px; -fx-background-color: #ECECEC; -fx-spacing: 15px;"
        }
    }

    private fun refreshView() {
        content.children.clear()
    }
}
