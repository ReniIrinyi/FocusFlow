package view.timeline

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.scene.control.CheckBox
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.shape.Circle
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.web.WebView
import javafx.util.Duration
import model.Task
import model.User
import controller.GenericController
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class TimeLine(private val user: User, private val taskController: GenericController<Task>) {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timelineContent = Pane()
    private val scrollPane = ScrollPane(timelineContent).apply { styleClass.add("timeline-scroll-pane") }
    private val nowPointer = Polygon(
        -7.0, -12.0,
        7.0, -12.0,
        0.0, 0.0
    ).apply {

    }

    private var windowStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
    private var windowEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0)


    private val pxPerMinute = 3.0
    private var totalHeight = 0.0
    val dayColors = mapOf(
        "Monday" to "#4CAF50",
        "Tuesday" to "#2196F3",
        "Wednesday" to "#ffffff",
        "Thursday" to "#FF9800",
        "Friday" to "#FFD700",
        "Saturday" to "#9C27B0",
        "Sunday" to "#F44336"
    )

    val currentDay = LocalDate.now().dayOfWeek.name.lowercase().capitalize()
    val backgroundColor = dayColors[currentDay] ?: "#4CAF50"

    fun createView(tasks: List<Task>): Pane {

        scrollPane.isPannable = false
        drawTimeMarkers()
        drawTasks(tasks)

        timelineContent.children.addAll(nowPointer)

        initTimeUpdater()
        initAutoScrollToCurrentTime()
        val header = createUserHeader(user)

        val scrollPane = VBox(scrollPane).apply {
            VBox.setVgrow(scrollPane, Priority.ALWAYS)
            HBox.setHgrow(scrollPane, Priority.ALWAYS)
        }
        return GridPane().apply {
            hgap = 0.0;
            vgap = 0.0;
            add(header, 0, 0)
            add(scrollPane, 0, 1)
            style = "-fx-background-color: $backgroundColor;"  // Dynamic background
        }
    }

    private fun initAutoScrollToCurrentTime() {
        val now = LocalDateTime.now()
        val diffMin = ChronoUnit.MINUTES.between(windowStart, now).toDouble()
        val currentPos = diffMin * pxPerMinute

        val scrollPosition = (currentPos / totalHeight).coerceIn(0.0, 1.0)

        scrollPane.vvalue = scrollPosition
    }


    /**
     * Zeichnet die Halbstunden-Markierungen und legt die Größe des timelineContent fest.
     */
    private fun drawTimeMarkers() {
        val totalMinutes = ChronoUnit.MINUTES.between(windowStart, windowEnd).toDouble()
        totalHeight = totalMinutes * pxPerMinute

        timelineContent.prefWidth = 600.0
        timelineContent.prefHeight = totalHeight
        timelineContent.style = "-fx-background-color: #f5f5f5;"


        val centerX = 300.0

        val pastLine = Rectangle(centerX - 5, 0.0, 10.0, totalHeight).apply {
            styleClass.add("past-line")
        }
        timelineContent.children.add(pastLine)

        val futureLine = Rectangle(centerX - 5, 0.0, 10.0, 0.0).apply {
            styleClass.add("future-line")
        }
        timelineContent.children.add(futureLine)

        var current = windowStart
        while (!current.isAfter(windowEnd)) {
            val diffMin = ChronoUnit.MINUTES.between(windowStart, current).toDouble()
            val yPos = diffMin * pxPerMinute

            val timeLabel = Label(current.format(timeFormatter)).apply {
                layoutX = centerX + 20
                layoutY = yPos - 10
                font = Font.font(14.0)
            }
            timelineContent.children.add(timeLabel)

            current = current.plusMinutes(30)
        }

        val timeline = Timeline(
            KeyFrame(Duration.seconds(1.0), EventHandler {
                val now = LocalDateTime.now()
                val diffMin = ChronoUnit.MINUTES.between(windowStart, now).toDouble()
                val currentPos = diffMin * pxPerMinute

                futureLine.height = totalHeight - currentPos
                futureLine.layoutY = currentPos
            })
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }


    private fun drawTasks(tasks: List<Task>) {
        val centerX = 300.0
        val taskWidth = 180.0
        val horizontalOffset = 50.0
        var placeOnLeft = true

        tasks.sortedBy { it.startTime }.forEach { t ->
            val start = requireNotNull(t.startTime)
            val end = requireNotNull(t.endTime)

            val startDiff = ChronoUnit.MINUTES.between(windowStart, start).toDouble()
            val endDiff = ChronoUnit.MINUTES.between(windowStart, end).toDouble()

            val startY = startDiff * pxPerMinute
            val endY = endDiff * pxPerMinute
            val height = (endY - startY).coerceAtLeast(30.0)

            val isPast = (end < LocalDateTime.now())
            val bgColorClass = if (isPast) "past-task" else "future-task"
            val bgColor = if (isPast) "#B0BEC5" else "#90CAF9"

            val imageView = t.imageBase64?.takeIf { it.isNotBlank() }?.let {
                val bytes = Base64.getDecoder().decode(it)
                ImageView(Image(ByteArrayInputStream(bytes))).apply {
                    fitWidth = taskWidth - 20
                    isPreserveRatio = true
                }
            }

            val webView = WebView().apply {
                prefHeight = 100.0 // Begrenze die Höhe, falls die Beschreibung zu lang ist
                prefWidth = taskWidth - 20.0
                engine.loadContent(t.description) // HTML-Inhalt der Aufgabe
            }

            val priorityLabel = Label("Priority: ${t.priority}").apply {
                style = when (t.priority) {
                    1 -> "-fx-text-fill: red; -fx-font-weight: bold;"
                    2 -> "-fx-text-fill: orange;"
                    3 -> "-fx-text-fill: green;"
                    else -> "-fx-text-fill: black;"
                }
            }

            val statusCheckBox = CheckBox().apply {
                isSelected = t.status == 0
                setOnAction {
                    if (isSelected) {
                        t.status = 2
                    } else {
                        t.status = 1
                    }
                }
            }



            val box = VBox().apply {
                styleClass.addAll("task-box", bgColorClass)
                layoutX = if (placeOnLeft) (centerX - taskWidth - 20) else (centerX + 20 + horizontalOffset)
                layoutY = startY
                prefWidth = taskWidth
                prefHeight = height
                imageView?.let { children.add(it) }
                children.addAll(
                    priorityLabel,
                    statusCheckBox,
                    Label(t.title).apply { styleClass.add("task-title") },
                    Label("${start.format(timeFormatter)} - ${end.format(timeFormatter)}")
                        .apply { styleClass.add("task-time") },
                    webView
                )
            }
            placeOnLeft = !placeOnLeft

            timelineContent.children.addAll(box)
        }
    }

    private fun createTriStateCheckbox(task: Task): Label {
        val checkBox = Label().apply {
            minWidth = 30.0
            minHeight = 30.0
            style = getStatusStyle(task.status)
            text = getStatusText(task.status)
            setOnMouseClicked {
                task.status = getNextStatus(task.status)
                style = getStatusStyle(task.status)
                text = getStatusText(task.status)
                saveTaskStatus(task)
            }
        }
        return checkBox
    }

    private fun saveTaskStatus(task: Task) {
        taskController.createRequest("PUT",null,null,task,null)
    }


    private fun getNextStatus(currentStatus: Int): Int {
        return when (currentStatus) {
            0 -> 1
            1 -> 2
            else -> 0
        }
    }

    private fun getStatusStyle(status: Int): String {
        return when (status) {
            0 -> "-fx-background-color: #FFEB3B; -fx-border-color: black; -fx-alignment: center;"
            1 -> "-fx-background-color: #64B5F6; -fx-border-color: black; -fx-alignment: center;"
            2 -> "-fx-background-color: #81C784; -fx-border-color: black; -fx-alignment: center;"
            else -> "-fx-background-color: #E0E0E0; -fx-border-color: black; -fx-alignment: center;"
        }
    }

    private fun getStatusText(status: Int): String {
        return when (status) {
            0 -> "O"
            1 -> "I"
            2 -> "✔"
            else -> "?"
        }
    }





    private fun initTimeUpdater() {
        val nowLine = Line(280.0, 0.0, 320.0, 0.0).apply {
            styleClass.add("now-line")
        }
        timelineContent.children.add(nowLine)

        val timeline = Timeline(
            KeyFrame(Duration.seconds(1.0), EventHandler {
                val now = LocalDateTime.now()
                val diffMin = ChronoUnit.MINUTES.between(windowStart, now).toDouble()
                val currentPos = diffMin * pxPerMinute

                nowLine.startY = currentPos
                nowLine.endY = currentPos
            })
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }

    private fun createUserHeader(user: User): HBox {
        val image = user.profileImage.takeIf { it.isNotBlank() }?.let {
            val bytes = Base64.getDecoder().decode(it)
            ImageView(Image(ByteArrayInputStream(bytes))).apply {

                fitWidth = 110.0
                fitHeight = 110.0
                isPreserveRatio = false

                val radius = fitWidth / 2
                val circleClip = Circle(radius, radius, radius)
                clip = circleClip
            }
        }

        return HBox().apply {
            alignment = javafx.geometry.Pos.CENTER_LEFT
            styleClass.add("user-header")
            image?.let { children.add(it) }
        }

    }
}
