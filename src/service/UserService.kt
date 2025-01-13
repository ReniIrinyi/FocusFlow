import model.User
import utils.Role
import java.io.File
import java.security.MessageDigest

class UserService {

    private val userFilePath = File("user_data.txt")

    init {
        checkIfFilePathExists()
        initializeCurrentId()
    }

    /**
     * Ensure the user file exists.
     */
    private fun checkIfFilePathExists() {
        if (!userFilePath.exists()) {
            println("File $userFilePath does not exist. Creating a new file...")
            userFilePath.createNewFile()
        } else {
            println("File $userFilePath exists.")
        }
    }

    /**
     * Initialize the current ID based on the users in the file.
     */
    private fun initializeCurrentId() {
        val users = getUsers()
        if (users.isNotEmpty()) {
            User.currentId = users.maxOf { it.id }
        }
    }

    /**
     * Save a new user.
     * If the user is an admin, ensure no other admin exists.
     */
    fun saveUser(name: String, email: String, password: String?, role: Role) {
        if (role == Role.ADMIN && isAdminExists()) {
            throw IllegalArgumentException("Only one admin can exist!")
        }

        val id = User.generateId()
        val hashedPassword = if (role == Role.ADMIN) hashPassword(password ?: "") else ""
        val user = User(id, name, email, hashedPassword, role)

        userFilePath.appendText("${user.id}|${user.name}|${user.email}|${user.password}|${user.role}\n")
        println("User ${user.name} with role ${user.role} saved successfully.")
    }

    /**
     * Update the admin's password.
     */
    fun updateAdminPassword(username: String, newPassword: String) {
        val users = userFilePath.readLines()
        val updatedUsers = users.map { line ->
            val parts = line.split("|")
            if (parts[1] == username && parts[4] == Role.ADMIN.name) {
                val hashedPassword = hashPassword(newPassword)
                "${parts[0]}|${parts[1]}|${parts[2]}|$hashedPassword|${parts[4]}"
            } else {
                line
            }
        }
        userFilePath.writeText(updatedUsers.joinToString("\n"))
    }

    /**
     * Validate user credentials.
     */
    fun validateUser(username: String, inputPassword: String?): Boolean {
        val users = userFilePath.readLines()
        val userLine = users.find { it.split("|")[1] == username } ?: return false

        val userData = userLine.split("|")
        val role = Role.valueOf(userData[4])

        return if (role == Role.ADMIN) {
            val savedPassword = userData[3]
            val inputHashedPassword = hashPassword(inputPassword ?: "")
            inputHashedPassword == savedPassword
        } else {
            true // Regular users do not require passwords
        }
    }


    /**
     * Update a user's details by ID.
     */
    fun updateUser(userId: Int, updatedName: String, updatedEmail: String, updatedRole: Role) {
        val users = getUsers()
        val updatedUsers = users.map { user ->
            if (user.id == userId) {
                    user.password
                user.copy(
                    name = updatedName,
                    email = updatedEmail,
                    role = updatedRole)
            } else {
                user
            }
        }

        if (users == updatedUsers) {
            println("User with ID $userId not found.")
            return
        }

        userFilePath.writeText(
            updatedUsers.joinToString("\n") { "${it.id}|${it.name}|${it.email}|${it.password}|${it.role}" }
        )
        println("User with ID $userId updated successfully.")
    }

    /**
     * Check if an admin already exists.
     */
    fun isAdminExists(): Boolean {
        return getUsers().any { it.role == Role.ADMIN }
    }

    /**
     * Retrieve the current admin user.
     */
    fun getAdmin(): User? {
        return getUsers().find { it.role == Role.ADMIN }
    }

    fun getUserById(userId: Int): User? {
        return getUsers().find { it.id == userId }
    }

    /**
     * Retrieve all users.
     */
    fun getUsers(): List<User> {
        return userFilePath.readLines()
            .mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 5) {
                    User(
                        id = parts[0].toInt(),
                        name = parts[1],
                        email = parts[2],
                        password = parts[3],
                        role = Role.valueOf(parts[4])
                    )
                } else {
                    null
                }
            }
    }

    /**
     * Delete a user by ID.
     */
    fun deleteUser(userId: Int) {
        val users = getUsers()
        val updatedUsers = users.filter { it.id != userId }

        if (users.size == updatedUsers.size) {
            println("User with ID $userId not found.")
            return
        }

        userFilePath.writeText(
            updatedUsers.joinToString("\n") { "${it.id}|${it.name}|${it.email}|${it.password}|${it.role}" }
        )
        println("User with ID $userId deleted successfully.")
    }

    /**
     * Hash a password using SHA-256.
     */
    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }
}
