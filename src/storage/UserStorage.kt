package storage

import model.User
import utils.Constants
import java.io.File
import java.security.MessageDigest

class UserStorage : StorageInterface<User> {

    private val filePath = Constants.USER_FILE_PATH
    private val file = File(filePath)

    init {
        checkIfFilePathExists()
        initializeCurrentId()
    }

    override  fun checkIfFilePathExists() {
        if (!file.exists()) {
            println("Datei $file existiert nicht. Eine neue Datei wird erstellt...")
            file.createNewFile()
        }
    }

    private fun initializeCurrentId() {
        val users = loadEntities().first
        if (users.isNotEmpty()) {
            User.currentId = users.maxOf { it.id }
        }
    }

    override fun create(entity: User, routePath: String?): Pair<Any, Int> {
        val (users, status) = loadEntities()
        val newUser = entity.copy(password = hashPassword(entity.password))
        val updatedUsers = users.toMutableList()
        updatedUsers.add(newUser)
        return Pair(saveEntities(updatedUsers), Constants.STATUS_OK)
    }

    override fun read(entityId: Int?, userId: Int?,newData:User?, routePath: String?): Pair<Any, Int> {
        val (users, status) = loadEntities()
        return when (routePath) {
            "all" -> Pair(users, status)
            "byId" -> {
                val user = users.find { it.id == entityId }
                if (user != null) Pair(user, Constants.STATUS_OK) else Pair("User not found", Constants.STATUS_NOT_FOUND)
            }
            "isAdminExists" -> Pair(isAdminExists(), Constants.STATUS_OK)
            "getAdmin" -> {
                val admin = getAdmin()
                if (admin != null) Pair(admin, Constants.STATUS_OK) else Pair("Admin user not found", Constants.STATUS_NOT_FOUND)
            }
            "validateUser" -> {
                val user = users.find { it.name == newData?.name }
                    ?: return Pair(false, Constants.STATUS_NOT_FOUND) // Benutzer existiert nicht.

                return when (user.role) {
                    Constants.ROLE_ADMIN -> {
                        val password = newData?.password ?: ""
                        val hashedInputPassword = hashPassword(password)
                        if (user.password == hashedInputPassword) {
                            Pair(true, Constants.STATUS_OK) // Passwort korrekt
                        } else {
                            Pair(false, Constants.STATUS_ERROR)
                        }
                    }
                    else -> {
                        // Reguläre Benutzer benötigen keine Passwortprüfung.
                        Pair(true, Constants.STATUS_OK)
                    }
                }
            }

            else -> Pair("Invalid route path", Constants.STATUS_BAD_REQUEST)
        }
    }

    override fun update(entityId: Int, updatedData: User, routePath: String?): Pair<Any, Int> {
        val (users, status) = loadEntities()
        return if (routePath == "updatePasswort") {
            val userIndex = users.indexOfFirst { it.role == Constants.ROLE_ADMIN }
            if (userIndex == -1) return Pair("Admin not found", Constants.STATUS_NOT_FOUND)
            val updatedUsers = users.toMutableList()
            val password = hashPassword(updatedData.password)
            updatedUsers[userIndex] = updatedUsers[userIndex].copy(password = password)
            return Pair(saveEntities(updatedUsers), Constants.STATUS_OK)
        } else {
            val userIndex = users.indexOfFirst { it.id == entityId }
            if (userIndex == -1) return Pair("User not found", Constants.STATUS_NOT_FOUND)
            val updatedUsers = users.toMutableList()
            updatedUsers[userIndex] = updatedData
            Pair(saveEntities(updatedUsers), Constants.STATUS_OK)
        }
    }

    override fun delete(entityId: Int, routePath: String?): Pair<Any, Int> {
        val (users, status) = loadEntities()
        val updatedUsers = users.filter { it.id != entityId }
        return Pair(saveEntities(updatedUsers), Constants.STATUS_OK)
    }

    override fun loadEntities(): Pair<List<User>, Int> {
        return try {
            val users = file.readLines().mapNotNull { line -> parseUser(line) }
            Pair(users, Constants.STATUS_OK)
        } catch (e: Exception) {
            Pair(emptyList(), Constants.STATUS_ERROR)
        }
    }

    override fun saveEntities(entities: List<User>): Int {
        return try {
            file.writeText(entities.joinToString("\n") { serializeUser(it) })
            Constants.STATUS_OK
        } catch (e: Exception) {
            Constants.STATUS_ERROR
        }
    }

    private fun serializeUser(user: User): String {
        return listOf(user.id, user.name, user.email, user.password, user.role, user.profileImage).joinToString("|")
    }

    private fun parseUser(line: String): User? {
        val tokens = line.split("|")
        return if (tokens.size == 6) {
            User(tokens[0].toInt(), tokens[1], tokens[2], tokens[3], tokens[4].toInt(), tokens[5])
        } else null
    }

    private fun isAdminExists(): Boolean {
        return loadEntities().first.any { it.role == Constants.ROLE_ADMIN }
    }

    private fun getAdmin(): User? {
        return loadEntities().first.find { it.role == Constants.ROLE_ADMIN }
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        return md.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}