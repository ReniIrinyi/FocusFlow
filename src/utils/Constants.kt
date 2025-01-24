package utils

object Constants {

    //Filepaths
    const val TASKS_FILE_PATH = "tasks.txt" // Der Standard-Datei für AufgabenDaten
    const val USER_FILE_PATH= "user_data.txt" //Standard-Datei für UserDaten
    const val TIMELINE_FILE_PATH ="timeline.txt" //Datei für Timeline - Settings

    // Statuswerte
    const val STATUS_NOT_DONE = 0
    const val STATUS_IN_PROGRESS = 1
    const val STATUS_DONE = 2

    // HTTP-ähnliche Statuscodes
    const val RESTAPI_OK = 200 // status response ok
    const val RESTAPI_NOT_FOUND = 404 //status response not found
    const val RESTAPI_INTERNAL_SERVER_ERROR = 500 //status response internal server_error
    const val RESTAPI_ADMIN_EXISTS = 506 //status admin existiert bereits
    const val RESTAPI_BAD_REQUEST = 400 // status bad request

    //RequestTypen
    const val GET = "GET"
    const val POST = "POST"
    const val PUT = "PUT"
    const val DELETE = "DELETE"

    //Prioritys
    const val PRIORITY_HIGH = 1
    const val PRIORITY_MEDIUM = 2
    const val PRIORITY_LOW = 3

    //User-Roles
    const val ROLE_ADMIN = 1
    const val ROLE_USER = 2
}