package view.timeline

import javafx.geometry.Insets
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.Task
import model.User
import controller.GenericController
import utils.Constants
import java.io.File

class TimeLineManager(
    private val taskController: GenericController<Task>,
    private val userController: GenericController<User>,
) {

    private val settingsFile = File(Constants.TIMELINE_FILE_PATH)

    fun createView(): VBox {
        val (timelineCount, selectedUserIds) = loadTimeLineSettings()

        val timelinesContainer = HBox(20.0).apply {
            padding = Insets(20.0)
            alignment = javafx.geometry.Pos.CENTER
        }

        selectedUserIds
            .filterNotNull()
            .take(timelineCount)
            .forEach { userId ->
                val user = userController.createRequest("GET",null,userId,null,"byId").first as User
                val tasks = taskController.createRequest("GET",null,userId,null,"byUserId").first as List<Task>

                if (user != null) {
                    val timeLineMenu = TimeLine(user, taskController)
                    val timelineBox = timeLineMenu.createView(tasks)
                    timelinesContainer.children.add(timelineBox)
                }
            }

        val header = Header()

        return VBox().apply {
            prefWidth = Double.MAX_VALUE
            prefHeight = Double.MAX_VALUE
            styleClass.add("timelineManager-root")
            children.addAll(header, timelinesContainer)
        }
    }



    private fun loadTimeLineSettings(): Pair<Int, List<Int?>> {
        if (settingsFile.exists()) {
            val content = settingsFile.readText()
            val parts = content.split("|")
            if (parts.isNotEmpty()) {
                val timelineCount = parts[0].toIntOrNull() ?: 1
                val userIds = parts.drop(1).map { it.toIntOrNull() }
                return Pair(timelineCount, userIds)
            }
        }
        return Pair(1, listOf(null)) // Default to one timeline with no user
    }

}
