package service

/**
 * Generisches CRUD-Interface, das grundlegende Operationen zur Verfügung stellt.
 *
 * @param T Der Typ der Entität (z. B. Task, User).
 */
interface CrudService<T> {

    /**
     * Ruft alle Entitäten ab.
     *
     * @return Eine Liste aller Entitäten.
     */
    fun findAll(): List<T>

    /**
     * Ruft eine spezifische Entität anhand ihrer ID ab.
     *
     * @param id Die eindeutige ID der Entität.
     * @return Die Entität, falls gefunden, ansonsten null.
     */
    fun findById(id: Int): T?

    /**
     * Speichert eine neue Entität oder aktualisiert eine bestehende.
     *
     * @param entity Die Entität, die gespeichert oder aktualisiert werden soll.
     */
    fun save(entity: T)

    /**
     * Löscht eine Entität anhand ihrer ID.
     *
     * @param id Die eindeutige ID der zu löschenden Entität.
     */
    fun delete(id: Int)
}