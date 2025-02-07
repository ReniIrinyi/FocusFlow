package controller

import storage.StorageInterface

/**
 * Generisches CRUD-Interface, das grundlegende Operationen zur Verfügung stellt.
 *
 * @param T Der Typ der Entität (z. B. Task, User).
 */
class GenericController<T>(private val storage: StorageInterface<T>) {

    fun create(entity: T, routePath: String? = null): Pair<Any, Int> {
        return storage.create(entity, routePath)
    }

    fun read(entityId: Int? = null, userId: Int? = null,newData:T? = null, routePath: String? = null): Pair<Any, Int> {
        return storage.read(entityId, userId, newData, routePath)
    }

    fun update(entityId: Int, updatedData: T, routePath: String? = null): Pair<Any, Int> {
        return storage.update(entityId, updatedData, routePath)
    }

    fun delete(entityId: Int, routePath: String? = null): Pair<Any, Int> {
        return storage.delete(entityId, routePath)
    }

}

