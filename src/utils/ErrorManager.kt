package utils

/**
 * Enthält Konstanten, die in der gesamten Anwendung genutzt werden.
 * Ziel:
 * - Zentralisierung von Literalwerten und Konfiguration.
 */
object ErrorManager {
    // Statuswerte
    const val STATUS_NOT_DONE = 0  // Nicht erledigt (Standardwert)
    const val STATUS_IN_PROGRESS = 1 // In Bearbeitung
    const val STATUS_DONE = 2            // Erledigt

    // HTTP-ähnliche Statuscodes
    const val RESTAPI_OK = 200 // status response ok
    const val RESTAPI_NOT_FOUND = 404 //status response not found
    const val RESTAPI_INTERNAL_SERVER_ERROR = 500 //status response internal server_error
}