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
    private val timeLineSettingsController: GenericController<TimeLineSettings>,
    private val helperFunctions: HelperFunctions)
{
    private val content = StackPane()

    init {
        val taskManagerMenu = TaskManagerMenu(taskController, userController)
        content.children.setAll(taskManagerMenu.createView())
    }

    fun createView(): VBox {
        val header = createAdminSettingsHeader()
        return VBox( header,content).apply {
            styleClass.add("adminMenu-container")
        }
    }

    private fun createAdminSettingsHeader(): HBox {
        val settingsBtn = Button("Einstellungen").apply {
            styleClass.add("custom-button")
            setOnAction {
                val adminSettingsMenu = AdminSettingsMenu(userController,timeLineSettingsController, helperFunctions) {
                    refreshView()
                }
                content.children.setAll(adminSettingsMenu.createView())
            }
        }

        val taskOverviewBtn = Button("Aufgabenübersicht").apply {
            styleClass.add("custom-button")
            setOnAction {
                val taskManagerMenu = TaskManagerMenu(taskController, userController)
                content.children.setAll(taskManagerMenu.createView())
            }
        }

        return HBox(10.0,taskOverviewBtn, settingsBtn).apply {
            styleClass.add("adminMenu-header")
        }
    }

    private fun refreshView() {
        content.children.clear()
    }
}
