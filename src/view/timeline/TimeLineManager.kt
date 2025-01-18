package view.timeline

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.Task
import service.TaskService
import service.UserService
import utils.Constants
import utils.HelperFunctions
import java.io.File

class TimeLineManager(
    private val taskService: TaskService,
    private val userService: UserService,
    private val helperFunctions: HelperFunctions,
) {

    private val settingsFile = File(Constants.TIMELINE_FILE_PATH)

    fun createView(): VBox {
        // Load settings (number of timelines and assigned users)
        val (timelineCount, selectedUserIds) = loadTimeLineSettings()

        // Generate timelines for valid user IDs
        val timelines = selectedUserIds.filterNotNull().take(timelineCount).mapNotNull { userId ->
            val user = userService.findById(userId)
            val tasks:List<Task> = taskService.findByUserId(userId)
            println(tasks.size)

            if (user != null) {
                val timeLineMenu = TimeLine()
                VBox(10.0).apply {
                    children.addAll(
                        Label("Timeline for: ${user.name}"), // Display the user's name
                        timeLineMenu.createView(tasks)
                    )
                }
            } else {
                null // Skip invalid user IDs
            }
        }

        // Arrange timelines horizontally
        val timelineBox = HBox(10.0).apply {
            padding = Insets(10.0)
            children.addAll(timelines)
        }

        return VBox(timelineBox).apply {
            padding = Insets(20.0)
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
