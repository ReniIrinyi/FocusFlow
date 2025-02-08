package utils

object Constants {

    //Filepaths
    const val TASKS_FILE_PATH = "tasks.txt" // Der Standard-Datei für AufgabenDaten
    const val USER_FILE_PATH= "user_data.txt" //Standard-Datei für UserDaten
    const val TIMELINE_FILE_PATH ="timeline.txt" //Datei für Timeline - Settings

    // Statuscodes für Errorhandling
    const val STATUS_OK = 200 // status response ok
    const val STATUS_NOT_FOUND = 404 //status response not found
    const val STATUS_ERROR = 500 //status response internal server_error
    const val STATUS_BAD_REQUEST = 400 // status bad request

    //User-Roles
    const val ROLE_ADMIN = 1
    const val ROLE_USER = 2
}