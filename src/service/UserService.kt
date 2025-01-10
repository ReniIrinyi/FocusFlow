package service

import model.User
import org.w3c.dom.Document
import utils.Constants
import utils.Role
import java.io.File
import java.security.MessageDigest

class UserService {
    init {
        checkIfFilePathExists();
    }

    private val userFilePath = File("user_data.txt")

    fun checkIfFilePathExists(){
        println("checkIfFilePathExists()...")
        println(userFilePath)
        if (userFilePath !== null && !userFilePath.exists()){
            println("File $userFilePath does not exist")
            File.createTempFile("user", "txt")
        } else {
            println("File $userFilePath exists")
        }
    }

    fun userExists(): Boolean {
        return userFilePath.exists()
    }

    fun saveUser(username: String, password: String) {
        val hashedPassword = hashPassword(password)
        val userData = "$username|$hashedPassword"
        userFilePath.writeText(userData)
    }

    fun validateUser(inputPassword: String): Boolean {
        val file = userFilePath
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