package controller

import model.TimeLineSettings
import utils.Constants
import java.io.File

class TimeLineSettingsStorage : StorageInterface<TimeLineSettings> {
    private val filePath = Constants.TIMELINE_FILE_PATH
    private val file = File(filePath)

    // Prüft, ob die Datei zum Speichern der Timeline-Einstellungen existiert
    override fun checkIfFilePathExists() {
        if (!file.exists()) {
            println("Datei $file existiert nicht. Eine neue Datei wird erstellt...")
            file.createNewFile() // Erstellt die Datei, falls sie nicht existiert
        }
    }

    // Lädt die Timeline-Einstellungen aus der Datei
    override fun loadEntities(): Pair<List<TimeLineSettings>, Int> {
        checkIfFilePathExists() // Sicherstellen, dass die Datei existiert

        // Dateiinhalt lesen und TimelinEinstellungen laden
        val content = file.readText().trim()
        if (content.isNotEmpty()) {
            val parts = content.split("|")
            val timelineCount = parts[0].toIntOrNull() ?: 1

            val timelines = parts.drop(1).mapIndexed { index, userId ->
                userId.toIntOrNull()?.let {
                    TimeLineSettings(timelineCount, it)
                }
            }.filterNotNull() // Entfernt null-Elemente

            return Pair(timelines, timelineCount)
        }

        // Standardwert, wenn die Datei leer oder ungültig ist
        return Pair(emptyList(), 1)
    }

    // Entfernt eine Timeline-Einstellung anhand der ID (nicht benötigt für diese Klasse)
    override fun deleteEntityById(id: Int): Int {
        println("Warnung: Entfernung von Entitäten wird nicht unterstützt.")
        return -1
    }

    // Fügt eine neue Timeline-Einstellung hinzu (nicht erforderlich für einfache Speicherung)
    override fun addEntity(newEntity: TimeLineSettings): Int {
        println("Warnung: Hinzufügen wird nicht einzeln unterstützt.")
        return -1
    }

    // Aktualisiert eine bestehende Timeline-Einstellung
    override fun updateEntity(id: Int, updatedData: TimeLineSettings): Int {
        val (entities, _) = loadEntities()
        val updatedEntities = entities.map {
            if (it.userId == id) updatedData else it
        }
        saveEntities(updatedEntities)
        return 1
    }

    // Speichert die Timeline-Einstellungen in einer Datei
    override fun saveEntities(entities: List<TimeLineSettings>): Int {
        checkIfFilePathExists() // Sicherstellen, dass die Datei existiert

        // Timeline-Daten formatieren und schreiben
        val timelineCount = entities.firstOrNull()?.timeLineCount ?: 1
        val content = "$timelineCount|${entities.joinToString("|") { it.userId.toString() }}"
        file.writeText(content)
        return 1
    }
}