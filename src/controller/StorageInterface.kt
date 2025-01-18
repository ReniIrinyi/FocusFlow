package controller

/**
 * Generisches `StorageInterface`, das für die Verwaltung von beliebigen Entitäten geeignet ist (z. B. Task, User).
 *
 * Dieses Interface bietet grundlegende CRUD-Operationen (Erstellen, Lesen, Aktualisieren, Löschen)
 * für eine generische Entität `T`.
 *
 * @param <T> Der Typ der Entität, die verwaltet wird, z. B. `Task`, `User`.
 */
interface StorageInterface<T> {

    fun checkIfFilePathExists()
    /**
     * Lädt alle Entitäten aus einer Datenquelle (z. B. Datei, Datenbank).
     *
     * @return Ein `Pair`, das aus folgenden Elementen besteht:
     *          - `List<T>`: Eine Liste der geladenen Entitäten vom Typ `T`.
     *          - `Int`: Ein HTTP-ähnlicher Statuscode, um den Erfolg oder Fehler des Ladevorgangs zu signalisieren:
     *              - `200`: Erfolgreiches Laden der Einträge.
     *              - `500`: Interner Fehler beim Laden.
     *
     * **Beispiel:**
     * ```kotlin
     * val (entities, status) = loadEntities()
     * if (status == 200) {
     *     println("Erfolgreich geladen: " + entities.joinToString())
     * } else {
     *     println("Fehler beim Laden der Entitäten")
     * }
     * ```
     */
    fun loadEntities(): Pair<List<T>, Int>

    /**
     * Speichert alle übergebenen Entitäten in einer Datenquelle (z. B. Datei, Datenbank).
     *
     * @param entities Die Liste der zu speichernden Entitäten.
     * @return Ein HTTP-ähnlicher Statuscode:
     *          - `200`: Erfolgreiches Speichern der Einträge.
     *          - `500`: Fehler beim Speichern der Einträge.
     *
     * **Beispiel:**
     * ```kotlin
     * val status = saveEntities(listOf(entity1, entity2))
     * if (status == 200) {
     *     println("Entitäten erfolgreich gespeichert!")
     * } else {
     *     println("Fehler beim Speichern der Entitäten")
     * }
     * ```
     */
    fun saveEntities(entities: List<T>): Int

    /**
     * Aktualisiert eine vorhandene Entität in der Datenquelle anhand ihrer eindeutigen ID.
     *
     * @param id Die ID der Entität, die aktualisiert werden soll.
     * @param updatedData Die neuen, aktualisierten Daten der Entität.
     * @return Ein HTTP-ähnlicher Statuscode:
     *          - `200`: Erfolgreiche Aktualisierung der Entität.
     *          - `404`: Die Entität mit der angegebenen ID wurde nicht gefunden.
     *          - `500`: Fehler beim Aktualisieren der Entität.
     *
     * **Beispiel:**
     * ```kotlin
     * val status = updateEntity(5, updatedTask)
     * if (status == 200) {
     *     println("Entität aktualisiert!")
     * } else if (status == 404) {
     *     println("Entität nicht gefunden!")
     * } else {
     *     println("Fehler beim Aktualisieren der Entität")
     * }
     * ```
     */
    fun updateEntity(id: Int, updatedData: T): Int

    /**
     * Fügt eine neue Entität hinzu und speichert sie in der Datenquelle.
     *
     * @param newEntity Die neue Entität, die hinzugefügt werden soll.
     * @return Ein HTTP-ähnlicher Statuscode:
     *          - `200`: Erfolgreiches Hinzufügen der neuen Entität.
     *          - `500`: Fehler beim Hinzufügen der Entität.
     *
     * **Beispiel:**
     * ```kotlin
     * val status = addEntity(newTask)
     * if (status == 200) {
     *     println("Neue Entität wurde erfolgreich hinzugefügt.")
     * } else {
     *     println("Fehler beim Hinzufügen der neuen Entität.")
     * }
     * ```
     */
    fun addEntity(newEntity: T): Int

    /**
     * Löscht eine vorhandene Entität aus der Datenquelle basierend auf ihrer eindeutigen ID.
     *
     * @param id Die ID der zu löschenden Entität.
     * @return Ein HTTP-ähnlicher Statuscode:
     *          - `200`: Erfolgreiches Löschen der Entität.
     *          - `404`: Die Entität mit der angegebenen ID wurde nicht gefunden.
     *          - `500`: Fehler beim Löschen der Entität.
     *
     * **Beispiel:**
     * ```kotlin
     * val status = deleteEntityById(42)
     * if (status == 200) {
     *     println("Entität erfolgreich gelöscht!")
     * } else if (status == 404) {
     *     println("Entität mit gegebener ID nicht gefunden!")
     * } else {
     *     println("Fehler beim Löschen der Entität")
     * }
     * ```
     */
    fun deleteEntityById(id: Int): Int
}