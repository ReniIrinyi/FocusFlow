package ui.timeline

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.stage.Stage
import model.Task
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.text.Font
import javafx.util.Duration
import java.io.ByteArrayInputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

class TimeLineMenu {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timeline = Timeline()
    private val centerPane = Pane()

    fun start(stage: Stage, tasks: List<Task>) {
        val root = BorderPane()
        root.style = "-fx-background-color: #FDF8E3;"

        drawTimeLine(centerPane)

        val currentTimeLine = Line().apply {
            startX = 700.0
            endX =50.0
            strokeWidth = 2.0
        }
        centerPane.children.add(currentTimeLine)

        drawTimeMarkers()

        drawTasks(tasks)

        timeline.cycleCount = Timeline.INDEFINITE
       /* timeline.keyFrames.add(KeyFrame(Duration.seconds(1.0), {
            val now = LocalTime.now()
            val paneHeight = centerPane.height
            val position = calculateTimePosition(now, paneHeight)

            currentTimeLine.startY = position
            currentTimeLine.endY = position
            currentTimeLine.toFront()
        }))*/

        timeline.play()

        root.center = centerPane
        val scene = Scene(root, 1200.0, 800.0)
        val css = javaClass.getResource("/utils/timeline.css")?.toExternalForm()
        if (css != null) {
            scene.stylesheets.add(css)
            println("CSS found: $css")
        } else {
            println("CSS not found!")
        }


        stage.title = "Zeitachse - Aufgaben"
        stage.scene = scene
        stage.show()
    }

    private fun drawTimeLine(centerPane: Pane) {
        val centerX = 600.0
        val startHour = 12
        val endHour = 23

        val pastTimeLine = Line().apply {
            startX = centerX
            endX = centerX
            startY = 0.0
            endY = 0.0
            styleClass.add("past-time-line")
        }

        val futureTimeLine = Line().apply {
            startX = centerX
            endX = centerX
            startY = 0.0
            endY = centerPane.height
            styleClass.add("future-time-line")

        }

        val currentTimePointer = Polygon().apply {
            points.addAll(
                centerX - 7.0, 0.0,
                centerX + 7.0, 0.0,
                centerX, 12.0
            )
            styleClass.add("current-time-pointer")
        }

        futureTimeLine.toFront()
        currentTimePointer.toFront()
        centerPane.children.addAll(pastTimeLine, futureTimeLine, currentTimePointer)

        timeline.cycleCount = Timeline.INDEFINITE
        timeline.keyFrames.add(KeyFrame(Duration.seconds(1.0), {
            val now = LocalTime.now()
            val paneHeight = centerPane.height
            val totalMinutes = (endHour - startHour) * 60.0
            val elapsedMinutes = ((now.hour - startHour) * 60) + now.minute
            val currentPosition = (elapsedMinutes / totalMinutes) * paneHeight

            pastTimeLine.endY = currentPosition
            futureTimeLine.startY = currentPosition
            futureTimeLine.endY = paneHeight

            currentTimePointer.translateY = currentPosition
        }))

        timeline.play()
    }


    private fun drawTimeMarkers() {
        val startHour = 12
        val endHour = 23
        val paneHeight = 800.0

        for (hour in startHour..endHour) {
            for (minute in 0..59) {
                val time = LocalTime.of(hour, minute)
                val yPos = calculateTimePosition(time, paneHeight)

                val dotSize = if (minute % 30 == 0) 2.0 else 0.0
                val dot = Circle(600.0, yPos, dotSize).apply {
                    fill = if (minute % 30 == 0) Color.WHITE else null
                }

                if (minute % 30 == 0) {
                    val timeLabel = Label(time.format(timeFormatter)).apply {
                        font = Font.font(14.0)
                        layoutX = 620.0
                        layoutY = yPos - 10.0
                    }
                    centerPane.children.addAll(dot, timeLabel)
                } else {
                    centerPane.children.add(dot)
                }
            }
        }
    }

    fun decodeBase64ToImage(base64String: String): Image {
        val imageBytes = Base64.getDecoder().decode(base64String)
        return Image(ByteArrayInputStream(imageBytes))
    }

    private fun drawTasks(tasks: List<Task>) {
        val taskSpacing = 20.0
        val taskWidth = 180.0
        val centerX = 600.0
        val horizontalOffset = 200.0
        val placedTasks = mutableListOf<Pair<Double, Double>>()
        var placeOnLeft = true

        tasks.sortedBy { it.startTime }.forEach { task ->
            val startY = calculateTimePosition(task.startTime ?: LocalTime.now(), 800.0)
            val endY = calculateTimePosition(task.endTime ?: LocalTime.now(), 800.0)
            val taskHeight = endY - startY

            val taskImage = task.imageBase64?.let { decodeBase64ToImage(it) }
            val imageView = taskImage?.let {
                ImageView(it).apply {
                    fitWidth = taskWidth - 20
                    isPreserveRatio = true
                }
            }

            var currentX = if (placeOnLeft) centerX - taskSpacing - taskWidth else centerX + 70
            while (placedTasks.any { (x, y) ->
                    x == currentX && y in startY..endY
                }) {
                currentX = if (placeOnLeft) currentX - horizontalOffset else currentX + horizontalOffset
                placeOnLeft = !placeOnLeft
            }

            val isPastTask = task.endTime?.isBefore(LocalTime.now()) ?: false
            val backgroundColor = if (isPastTask) "#B0BEC5" else "#90CAF9"

            val taskBlock = VBox().apply {
                layoutX = currentX
                layoutY = startY
                prefWidth = taskWidth
                prefHeight = taskHeight
                style = "-fx-background-color: $backgroundColor; -fx-border-color: #1976D2; -fx-border-radius: 5px; -fx-padding: 5px;"
                if (imageView != null) {
                    children.add(imageView)
                }
                children.addAll(
                    Label(task.title).apply { font = Font.font(14.0) },
                    Label("${task.startTime} - ${task.endTime}").apply { font = Font.font(12.0) }
                )
            }

            placedTasks.add(Pair(currentX, startY))

            placeOnLeft = !placeOnLeft

            centerPane.children.add(taskBlock)
        }
    }


    private fun calculateTimePosition(time: LocalTime, paneHeight: Double): Double {
        val startHour = 12
        val endHour = 23
        val totalMinutes = (endHour - startHour) * 60.0
        val pixelsPerMinute = paneHeight / totalMinutes
        val elapsedMinutes = ((time.hour - startHour) * 60) + time.minute
        return elapsedMinutes * pixelsPerMinute
    }
}