package utils

import javafx.scene.control.Alert
import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

 class HelperFunctions {

    fun encodeImageToBase64(filePath: String):String {
        return try {
            val imageBytes = Files.readAllBytes(Paths.get(filePath))
            return Base64.getEncoder().encodeToString(imageBytes)
        } catch (e: Exception) {
            println("Fehler: ${e.message}")
            return ""
        }
    }

    fun decodeBase64ToImage(base64String: String): Image? {
        return try {
            val imageBytes = Base64.getDecoder().decode(base64String)
             return Image(ByteArrayInputStream(imageBytes))
        } catch (e: Exception) {
            println("Fehler: ${e.message}")
            null
        }
    }

     /**
     * Zeigt ein Benachrichtigungs-Fenster dem Benutzer.
     *
     * @param type Art der Benachrichtigung (z. B. INFORMATION, WARNUNG).
     * @param title Titel des Benachrichtigungs-Fensters.
     * @param message Nachricht im Benachrichtigungs-Fenster.
     */
     fun showAlert(type: Alert.AlertType, title: String, message: String) {
        Alert(type).apply {
            this.title = title // Setzt den Titel der Benachrichtigung
            this.contentText = message // Setzt den Inhalt der Benachrichtigung
            showAndWait() // Zeigt die Benachrichtigung an und wartet
        }
    }
}
