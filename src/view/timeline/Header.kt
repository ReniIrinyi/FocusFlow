package view.timeline

import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class Header : HBox() {

    private val dayView = ImageView().apply {
        isPreserveRatio = true
        fitHeight = 120.0
        fitWidth = 280.0
    }

    private val periodImageView = ImageView().apply {
        isPreserveRatio = true
        fitHeight = 120.0
        fitWidth = 280.0
    }

    private val monthImageView = ImageView().apply {
        isPreserveRatio = true
        fitHeight = 120.0
        fitWidth = 280.0
    }

    init {
        styleClass.add("timeLineManager-header")
        alignment = Pos.CENTER_LEFT

        // Spacer to push the day image to the right
        val spacer1 = Region().apply {
            setHgrow(this, Priority.ALWAYS)
        }
        val spacer2 = Region().apply {
            setHgrow(this, Priority.ALWAYS)
        }

        dayView.image = Image(javaClass.getResourceAsStream(getDayImagePath()))
        periodImageView.image = Image(javaClass.getResourceAsStream(getPeriodImagePath()))
        monthImageView.image = Image(javaClass.getResourceAsStream(getMonthImagePath()))

        children.addAll(periodImageView, spacer1, dayView, spacer2, monthImageView)
    }

    private fun getDayImagePath(): String {
        val currentDay = LocalDate.now().dayOfWeek.name.lowercase().capitalize()
        return when (currentDay) {
            "Monday" -> "/images/montag.jpg"
            "Tuesday" -> "/images/dienstag.jpg"
            "Wednesday" -> "/images/mittwoch.jpg"
            "Thursday" -> "/images/donnerstag.jpg"
            "Friday" -> "/images/freitag.jpg"
            "Saturday" -> "/images/samstag.jpg"
            "Sunday" -> "/images/sonntag.jpg"
            else -> "/images/sonntag.jpg"
        }
    }

    private fun getPeriodImagePath(): String {
        val currentHour = LocalDateTime.now(java.time.ZoneId.systemDefault()).hour
        return when {
            currentHour in 6..11 -> "/images/morgen.jpg"
            currentHour in 12..13 -> "/images/mittag.jpg"
            currentHour in 18..< 21 -> "/images/abend.jpg"
            else -> "/images/nacht.jpg"
        }
    }

    private fun getMonthImagePath(): String {
        val currentMonth = LocalDateTime.now(java.time.ZoneId.systemDefault()).month
        println(currentMonth)
        return when (currentMonth) {
            Month.JANUARY -> "/images/januar.jpg"
            Month.FEBRUARY -> "/images/februar.jpg"
            Month.MARCH -> "/images/maerz.jpg"
            Month.APRIL -> "/images/april.jpg"
            Month.MAY -> "/images/mai.jpg"
            Month.JUNE -> "/images/juni.jpg"
            Month.JULY -> "/images/juli.jpg"
            Month.AUGUST -> "/images/august.jpg"
            Month.SEPTEMBER -> "/images/september.jpg"
            Month.OCTOBER -> "/images/oktober.jpg"
            Month.NOVEMBER -> "/images/november.jpg"
            Month.DECEMBER -> "/images/dezember.jpg"
            else -> "/images/januar.jpg"
        }
    }

}
