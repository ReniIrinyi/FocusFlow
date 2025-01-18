package view.timeline
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.util.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

//todo: warum so langsam..
class TimeLineHeader : HBox() {
    private val timeLabel = Label().apply {
        textFill = Color.WHITE
        font = Font.font(16.0)
    }
    init {
        background = Background(
            BackgroundFill(Color.rgb(76, 175, 80), CornerRadii(0.0), Insets.EMPTY)
        )
        spacing = 20.0
        alignment = Pos.CENTER_LEFT
        padding = Insets(10.0)

        val userLabel = Label("Guten Tag,\nIsabel").apply {
            textFill = Color.WHITE
            font = Font.font(16.0)
        }

       /* val userIcon = ImageView(Image("https://via.placeholder.com/50")).apply {
            fitWidth = 50.0
            fitHeight = 50.0
            isPreserveRatio = true
        }*/

        val userBox = VBox(userLabel).apply {
            spacing = 5.0
            alignment = Pos.CENTER
        }

        val dateLabel = Label("Heute ist Mittwoch, den 18.\nDezember, 2024").apply {
            textFill = Color.WHITE
            font = Font.font(16.0)
        }

       /* val sunIcon = ImageView(Image("https://via.placeholder.com/50")).apply {
            fitWidth = 50.0
            fitHeight = 50.0
            isPreserveRatio = true
        }
        val flowerIcon = ImageView(Image("https://via.placeholder.com/50")).apply {
            fitWidth = 50.0
            fitHeight = 50.0
            isPreserveRatio = true
        }*/

        val weatherBox = HBox().apply {
            spacing = 10.0
            alignment = Pos.CENTER
        }

        children.addAll(userBox, dateLabel, weatherBox, timeLabel)
        startClock()
    }

    private fun startClock() {
        val timeline = Timeline(
            KeyFrame(Duration.seconds(1.0), EventHandler {
                val now = LocalDateTime.now()
                timeLabel.text = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
            })
        )
        timeline.cycleCount = Timeline.INDEFINITE
        timeline.play()
    }
}