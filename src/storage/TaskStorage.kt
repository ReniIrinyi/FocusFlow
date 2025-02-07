package storage

import model.Task
import utils.Constants
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TaskStorage : StorageInterface<Task> {

    private val filePath = Constants.TASKS_FILE_PATH
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

    override fun create(entity: Task, routePath: String?): Pair<Any, Int> {
        val (tasks, status) = loadEntities()
        val updatedTasks = tasks.toMutableList()
        updatedTasks.add(entity)
        return Pair(saveEntities(updatedTasks), Constants.STATUS_OK)
    }

    override fun read(entityId: Int?, userId: Int?,newData:Task?, routePath: String?): Pair<Any, Int> {
        val (tasks, status) = loadEntities()
        return when (routePath) {
            "all" -> Pair(tasks, status)
            "byId" -> tasks.find { it.id == entityId }?.let { Pair(it, Constants.STATUS_OK) } ?: Pair("Task not found", Constants.STATUS_NOT_FOUND)
            "byUserId" -> {
                val userTasks = tasks.filter { it.userId == userId }
                if (userTasks.isNotEmpty()) Pair(userTasks, Constants.STATUS_OK) else Pair(emptyList<Task>(), Constants.STATUS_NOT_FOUND)
            }
            else -> Pair("Invalid route path", Constants.STATUS_BAD_REQUEST)
        }
    }

    override fun update(entityId: Int, updatedData: Task, routePath: String?): Pair<Any, Int> {
        val (tasks, status) = loadEntities()
        val taskIndex = tasks.indexOfFirst { it.id == entityId }
        if (taskIndex == -1) return Pair("Task not found", Constants.STATUS_NOT_FOUND)
        val updatedTasks = tasks.toMutableList()
        updatedTasks[taskIndex] = updatedData
        return Pair(saveEntities(updatedTasks), Constants.STATUS_OK)
    }

    override fun delete(entityId: Int, routePath: String?): Pair<Any, Int> {
        val (tasks, status) = loadEntities()
        val updatedTasks = tasks.filter { it.id != entityId }
        return Pair(saveEntities(updatedTasks), Constants.STATUS_OK)
    }

    override fun loadEntities(): Pair<List<Task>, Int> {
        return try {
            if (file.readText().isEmpty()) {
                return Pair(emptyList(), Constants.STATUS_OK)
            }
            val tasks = file.readLines().map { line -> parseTask(line) }
            Pair(tasks, Constants.STATUS_OK)
        } catch (e: Exception) {
            Pair(emptyList(), Constants.STATUS_ERROR)
        }
    }

    override fun saveEntities(entities: List<Task>): Int {
        return try {
            val lines = entities.map { serializeTask(it) }
            file.writeText(lines.joinToString("\n"))
            Constants.STATUS_OK
        } catch (e: Exception) {
            Constants.STATUS_ERROR
        }
    }


    /**
     * Serialisiert ein Task-Objekt in ein String-Format für die Speicherung in der Datei.
     */
    private fun serializeTask(task: Task): String {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        return listOf(
            task.id,
            task.userId,
            task.createdAt.format(formatter),
            task.updatedAt.format(formatter),
            task.priority,
            task.status,
            escapeField(task.title),
            escapeField(task.description),
            task.startTime.format(formatter),
            task.deadline?.format(formatter) ?: "",
            task.endTime.format(formatter),
            task.imageBase64
        ).joinToString("|")
    }

    /**
     * Parst eine Zeile aus der Datei in ein Task-Objekt.
     */
    private fun parseTask(line: String): Task {
        val tokens = line.split("|")
        return Task(
            id = tokens[0].toInt(),
            userId = tokens[1].toInt(),
            createdAt = LocalDateTime.parse(tokens[2]),
            updatedAt = LocalDateTime.parse(tokens[3]),
            priority = tokens[4].toInt(),
            status = tokens[5].toInt(),
            title = unescapeField(tokens[6]),
            description = unescapeField(tokens[7]),
            startTime = LocalDateTime.parse(tokens[8]),
            deadline = tokens[9].takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            endTime = LocalDateTime.parse(tokens[10]),
            imageBase64 = tokens[11]
        )
    }

    /**
     * Kodiert problematische Zeichen im Feld, damit sie sicher gespeichert werden können.
     */
    private fun escapeField(field: String): String {
        return field.replace("\\", "\\\\").replace("|", "\\|").replace("\n", "\\n")
    }

    /**
     * Dekodiert die gespeicherten Zeichen zurück in ihr Originalform.
     */
    private fun unescapeField(field: String): String {
        return field.replace("\\n", "\n").replace("\\|", "|").replace("\\\\", "\\")
    }


}