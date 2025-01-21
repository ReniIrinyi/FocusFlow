package view.timeline

import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import javafx.scene.layout.GridPane.setFillWidth
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import model.Task
import model.User
import service.TaskService
import service.UserService
import utils.Constants
import utils.HelperFunctions
import java.io.ByteArrayInputStream
import java.io.File
import java.util.*

class TimeLineManager(
    private val taskService: TaskService,
    private val userService: UserService,
    private val helperFunctions: HelperFunctions,
) {

    private val settingsFile = File(Constants.TIMELINE_FILE_PATH)

    fun createView(): VBox {
        val (timelineCount, selectedUserIds) = loadTimeLineSettings()

        // HBox a timeline-ok számára
        val timelinesContainer = HBox(20.0).apply {
            padding = Insets(20.0)
            alignment = javafx.geometry.Pos.CENTER
        }

        // Ha mégis szeretnéd, hogy automatikusan törjenek, akkor HBox helyett:
        // val timelinesContainer = FlowPane(20.0, 20.0).apply { ... }

        selectedUserIds
            .filterNotNull()
            .take(timelineCount)
            .forEach { userId ->
                val user = userService.findById(userId)
                val tasks: List<Task> = taskService.findByUserId(userId)

                if (user != null) {
                    val timeLineMenu = TimeLine(user)
                    // Egy VBox (vagy akármilyen layout) a fejléccel és a timeline-nal:
                    val timelineBox = timeLineMenu.createView(tasks)

                    // Hozzáadjuk a HBox-unkhoz
                    timelinesContainer.children.add(timelineBox)
                }
            }

        // Fejléc
        val header = TimeLineHeader()

        return VBox().apply {
            prefWidth = Double.MAX_VALUE
            prefHeight = Double.MAX_VALUE
            styleClass.add("timeline-background")
            // A VBox-ba beillesztjük a fejlécket és alatta a HBox-ot
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
