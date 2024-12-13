package utils

/**
 * Enthält Konstanten, die in der gesamten Anwendung genutzt werden.
 * Ziel:
 * - Zentralisierung von Literalwerten und Konfiguration.
 */
object Constants {
    const val TASKS_FILE_NAME = "tasks.txt" // Der Standard-Dateiname für die Aufgabenliste.

    // Statuswerte
    val STATUS_NOT_DONE = null  // Nicht erledigt (Standardwert)
    const val STATUS_DONE = 1            // Erledigt
    const val STATUS_IN_PROGRESS = 0 // In Bearbeitung

    // HTTP-ähnliche Statuscodes
    const val RESTAPI_OK = 200 // status response ok
    const val RESTAPI_NOT_FOUND = 404 //status response not found
    const val RESTAPI_INTERNAL_SERVER_ERROR = 500 //status response internal server_error
}