package storage

import model.TimeLineSettings
import utils.Constants
import java.io.File

class TimeLineSettingsStorage : StorageInterface<TimeLineSettings> {
    private val filePath = Constants.TIMELINE_FILE_PATH
    private val file = File(filePath)

    init {
        checkIfFilePathExists()
    }


    override fun checkIfFilePathExists() {
        if (!file.exists()) {
            println("Datei $file existiert nicht. Eine neue Datei wird erstellt...")
            file.createNewFile()
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
                if(status != Constants.RESTAPI_OK){
                    return Pair(emptyList<TimeLineSettings>(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
                } else {
                    return Pair(timeline, status)
                }
            }
            Constants.PUT->{
                if(newData != null && Id != null) {
                    val result = this.updateEntity(Id, newData)
                    if(result == Constants.RESTAPI_OK){
                        Pair("Timeline erfolreich aktualisiert", result)
                    } else {
                        Pair(emptyList<TimeLineSettings>(), Constants.RESTAPI_INTERNAL_SERVER_ERROR)
                    }
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

    override fun loadEntities(): Pair<List<TimeLineSettings>, Int> {
        val content = file.readText().trim()
        if (content.isEmpty()) {
            return Pair(emptyList(), Constants.RESTAPI_OK)
        }

        val parts = content.split("|")
        val timelineCount = parts[0].toIntOrNull() ?: 0
        val userIdParts = parts.drop(1)

        val realCount = minOf(timelineCount, userIdParts.size)

        val timelines = (0 until realCount).map { index ->
            val userId = userIdParts[index].toIntOrNull() ?: -1
            TimeLineSettings(timelineCount, userId)
        }

        return Pair(timelines, Constants.RESTAPI_OK)
    }





    override fun updateEntity(id: Int, updatedData: TimeLineSettings): Int {
        val (entityList, status) = loadEntities()
        val entities = entityList.toMutableList()

        val neededSize = updatedData.timeLineCount
        while (entities.size < neededSize) {
            entities.add(TimeLineSettings(neededSize, -1))
        }

        if (id < 0 || id >= neededSize) {
            return Constants.RESTAPI_BAD_REQUEST
        }

        entities[id] = updatedData

        saveEntities(entities)
        return Constants.RESTAPI_OK
    }


    override fun saveEntities(entities: List<TimeLineSettings>): Int {
        if (entities.isEmpty()) {
            file.writeText("")
            return Constants.RESTAPI_OK
        }
        val timelineCount = entities.first().timeLineCount
        val finalList = entities.take(timelineCount)

        val userIds = finalList.joinToString("|") { it.userId.toString() }
        val content = "$timelineCount|$userIds"

        file.writeText(content)
        return Constants.RESTAPI_OK
    }


    override fun deleteEntityById(id: Int): Int {
        return -1
    }

    override fun addEntity(newEntity: TimeLineSettings): Int {
        return -1
    }

}