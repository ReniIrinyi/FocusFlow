package storage

/**
 * Generisches `StorageInterface`, das für die Verwaltung von beliebigen Entitäten geeignet ist (z. B. Task, User).
 *
 * Dieses Interface bietet grundlegende CRUD-Operationen (Erstellen, Lesen, Aktualisieren, Löschen)
 * für eine generische Entität `T`.
 *
 * @param <T> Der Typ der Entität, die verwaltet wird, z. B. `Task`, `User`.
 */
interface StorageInterface<T> {

    fun create(entity: T, routePath: String? = null): Pair<Any, Int>

    fun read(entityId: Int?, userId: Int?,newData:T?, routePath: String? = null): Pair<Any, Int>

    fun update(entityId: Int, updatedData: T, routePath: String? = null): Pair<Any, Int>

    fun delete(entityId: Int, routePath: String? = null): Pair<Any, Int>

    fun loadEntities(): Pair<List<T>, Int>

    fun saveEntities(entities: List<T>): Int

    fun checkIfFilePathExists()

}