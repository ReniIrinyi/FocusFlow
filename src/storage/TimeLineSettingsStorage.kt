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

    override fun create(entity: TimeLineSettings, routePath: String?): Pair<Any, Int> {
        return Pair("Adding new TimeLineSettings is not supported.", Constants.STATUS_BAD_REQUEST)
    }

    override fun read(entityId: Int?, userId: Int?,newData:TimeLineSettings?, routePath: String?): Pair<Any, Int> {
        val (timelines, status) = loadEntities()
        return if (status == Constants.STATUS_OK) {
            Pair(timelines, status)
        } else {
            Pair(emptyList<TimeLineSettings>(), Constants.STATUS_ERROR)
        }
    }

    override fun update(entityId: Int, updatedData: TimeLineSettings, routePath: String?): Pair<Any, Int> {
        val (entityList, status) = loadEntities()
        val entities = entityList.toMutableList()

        val neededSize = updatedData.timeLineCount
        while (entities.size < neededSize) {
            entities.add(TimeLineSettings(neededSize, -1))
        }

        if (entityId < 0 || entityId >= neededSize) {
            return Pair("Invalid entity ID", Constants.STATUS_BAD_REQUEST)
        }

        entities[entityId] = updatedData

        return if (saveEntities(entities) == Constants.STATUS_OK) {
            Pair("Timeline successfully updated", Constants.STATUS_OK)
        } else {
            Pair("Error updating Timeline", Constants.STATUS_ERROR)
        }
    }

    override fun delete(entityId: Int, routePath: String?): Pair<Any, Int> {
        return Pair("Deleting TimelineSettings is not supported", Constants.STATUS_BAD_REQUEST)
    }

    override fun loadEntities(): Pair<List<TimeLineSettings>, Int> {
        val content = file.readText().trim()
        if (content.isEmpty()) {
            return Pair(emptyList(), Constants.STATUS_OK)
        }

        val parts = content.split("|")
        val timelineCount = parts[0].toIntOrNull() ?: 0
        val userIdParts = parts.drop(1)

        val realCount = minOf(timelineCount, userIdParts.size)

        val timelines = (0 until realCount).map { index ->
            val userId = userIdParts[index].toIntOrNull() ?: -1
            TimeLineSettings(timelineCount, userId)
        }

        return Pair(timelines, Constants.STATUS_OK)
    }

    override fun saveEntities(entities: List<TimeLineSettings>): Int {
        if (entities.isEmpty()) {
            file.writeText("")
            return Constants.STATUS_OK
        }
        val timelineCount = entities.first().timeLineCount
        val finalList = entities.take(timelineCount)

        val userIds = finalList.joinToString("|") { it.userId.toString() }
        val content = "$timelineCount|$userIds"

        file.writeText(content)
        return Constants.STATUS_OK
    }


}