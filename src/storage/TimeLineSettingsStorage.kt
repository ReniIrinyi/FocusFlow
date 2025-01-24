package storage

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

    /**
     * Behandelt verschiedene HTTP-Anfragen für Timeline-Einstellungen.
     *
     * Unterstützte Anfragetypen:
     * - **GET**: Ruft alle Timeline-Einstellungen ab.
     *      - Antwort: Liste aller Timeline-Einstellungen oder Fehlermeldung.
     *
     * - **PUT**: Aktualisiert eine bestehende Timeline-Einstellung mit den bereitgestellten Daten.
     *      - Erforderlich: ID der Timeline und neue Daten (`newData`).
     *      - Antwort: Erfolgs- oder Fehlermeldung mit Statuscode.
     *
     * - **DELETE**: Wird nicht unterstützt.
     *      - Antwort: Fehlermeldung mit Statuscode.
     *
     * - **POST**: Wird nicht unterstützt.
     *      - Antwort: Fehlermeldung mit Statuscode.
     *
     * @param routePath Der spezifische Pfad der Anfrage (z.B. "all", "byId").
     * @param requestTyp Der Typ der HTTP-Anfrage (GET, PUT, DELETE, POST).
     * @param Id Die eindeutige ID der Timeline (nur für PUT-Anfragen erforderlich).
     * @param userId Benutzer-ID für benutzerbezogene Anfragen (optional, aktuell nicht verwendet).
     * @param newData Die neuen Daten für PUT-Anfragen (optional).
     * @return Ein Paar bestehend aus der Antwort (Ergebnis oder Fehlermeldung) und dem HTTP-Statuscode.
     */
    override fun getRequest(
        requestTyp: String,
        Id: Int?,
        userId: Int?,
        newData: TimeLineSettings?,
        routePath: String?
    ): Pair<Any, Int> {
        return when (requestTyp) {
            Constants.GET->{
                val (timeline, status) = this.loadEntities();
                if(status !== Constants.RESTAPI_OK){
                    return Pair(emptyList<TimeLineSettings>(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
                } else {
                    return Pair(timeline, status)
                }
            }
            Constants.PUT->{
                if(newData != null && Id!=null) {
                    //todo: muss timeLineId einsetzen!
                    val result = this.updateEntity(Id, newData)
                    Pair("Timeline erfolreich aktualisiert", result)
                } else {
                    Pair("Fehler: Keine Daten zum Aktualisieren angegeben", Constants.RESTAPI_BAD_REQUEST)
                }
            }
            Constants.DELETE->{
                    Pair("Fehler: Delete von Timeline wird nicht unterstützt", Constants.RESTAPI_BAD_REQUEST)
            }
            Constants.POST->{
                    Pair("Hinzufügen von neuen Timeline wird nicht unterstützt.", Constants.RESTAPI_BAD_REQUEST)
            }
            else -> Pair("Ungüliter Anfrage-Typ", Constants.RESTAPI_BAD_REQUEST)
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

    override fun deleteEntityById(id: Int): Int {
        return -1
    }

    override fun addEntity(newEntity: TimeLineSettings): Int {
        return -1
    }
}