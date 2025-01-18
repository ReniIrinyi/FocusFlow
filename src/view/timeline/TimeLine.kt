package view.timeline

import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.control.ScrollPane
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.shape.Line
import javafx.scene.shape.Polygon
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font
import javafx.scene.web.WebView
import javafx.util.Duration
import model.Task
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

class TimeLine {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val timelineContent = Pane()
    private val scrollPane = ScrollPane(timelineContent)
    private val nowPointer = Polygon(-7.0, -12.0,
        7.0, -12.0,
        0.0,  0.0).apply { fill = Color.RED }

    private var windowStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0)
    private var windowEnd = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0)


    private val pxPerMinute = 3.0
    private var totalHeight = 0.0

    fun createView(tasks: List<Task>): Pane {
        scrollPane.isPannable = true
        scrollPane.hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
        scrollPane.vbarPolicy = ScrollPane.ScrollBarPolicy.ALWAYS
        scrollPane.style = "-fx-background-color: #FDF8E3;"
        scrollPane.setFitToWidth(true)
        scrollPane.setFitToHeight(false)

        drawTimeMarkers()
        drawTasks(tasks)

        timelineContent.children.addAll(nowPointer)

        initTimeUpdater()
        initAutoScrollToCurrentTime()
        val header = TimeLineHeader()

        val root = VBox(header,scrollPane).apply {
            setPrefSize(800.0, 600.0)
            spacing = 10.0
            padding = Insets(0.00,0.00,10.00,0.00)
        }
        return root
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

        val centerX = 300.0

        val pastLine = Rectangle(centerX - 5, 0.0, 10.0, totalHeight).apply {
            fill = Color.LIGHTGRAY
            arcWidth = 10.0
            arcHeight = 10.0
        }
        timelineContent.children.add(pastLine)

        val futureLine = Rectangle(centerX - 5, 0.0, 10.0, 0.0).apply {
            fill = Color.ORANGE
            arcWidth = 10.0
            arcHeight = 10.0
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

            val box = VBox().apply {
                layoutX = if (placeOnLeft) (centerX - taskWidth - 20) else (centerX + 20 + horizontalOffset)
                layoutY = startY
                prefWidth = taskWidth
                prefHeight = height
                style = """
                -fx-background-color: $bgColor;
                -fx-border-color: #1976D2;
                -fx-border-radius: 5px;
                -fx-padding: 5px;
            """.trimIndent()

                imageView?.let { children.add(it) }
                children.addAll(
                    Label(t.title).apply { font = Font.font(14.0) },
                    Label("${start.format(timeFormatter)} - ${end.format(timeFormatter)}")
                        .apply { font = Font.font(12.0) },
                    webView
                )
            }
            placeOnLeft = !placeOnLeft

            timelineContent.children.add(box)
        }
    }


    private fun initTimeUpdater() {
        val nowLine = Line(295.0, 0.0, 305.0, 0.0).apply {
            stroke = Color.BLUE
            strokeWidth = 4.0
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

}
