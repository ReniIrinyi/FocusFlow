package service

import model.User
import utils.Constants
import utils.Role
import java.io.File
import java.security.MessageDigest

class UserService {

    private val userFilePath = "user_data.txt"

    fun userExists(): Boolean {
        return File(userFilePath).exists()
    }

    fun saveUser(username: String, password: String) {
        val hashedPassword = hashPassword(password)
        val userData = "$username|$hashedPassword"
        File(userFilePath).writeText(userData)
    }

    fun validateUser(inputPassword: String): Boolean {
        val file = File(userFilePath)
        if (!file.exists()) return false

        val userData = file.readText().split("|")
        if (userData.size < 2) return false

        val savedPassword = userData[1]
        val inputHashedPassword = hashPassword(inputPassword)
        return inputHashedPassword == savedPassword
    }

    private fun hashPassword(password: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val hashedBytes = md.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }

}