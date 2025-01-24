package controller

import storage.StorageInterface
import utils.Constants

/**
 * Generisches CRUD-Interface, das grundlegende Operationen zur Verfügung stellt.
 *
 * @param T Der Typ der Entität (z. B. Task, User).
 */
class GenericController<T>(private val storage: StorageInterface<T>) {
    fun createRequest(
        requestTyp: String,
        entityId: Int? = null,
        userId: Int? = null,
        newData: T? = null,
        routePath: String? = null
    ): Pair<Any, Int> {
        return when (requestTyp) {
            Constants.GET -> storage.getRequest(Constants.GET, entityId, userId, newData, routePath)
            Constants.POST -> storage.getRequest(Constants.POST, entityId, userId, newData, routePath)
            Constants.PUT -> storage.getRequest(Constants.PUT, entityId, userId, newData, routePath)
            Constants.DELETE -> storage.getRequest(Constants.DELETE, entityId, userId, newData, routePath)
            else -> Pair("Ungültiger Anfrage-Typ.", Constants.RESTAPI_BAD_REQUEST)
        }
    }
}