package utils

import javafx.scene.image.Image
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

class ImgHandler {

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
}
