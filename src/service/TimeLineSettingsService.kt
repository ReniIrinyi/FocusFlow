package service

import controller.TimeLineSettingsStorage
import model.TimeLineSettings

/**
 * Service für die Verwaltung und den Zugriff auf die TimelineSettings-Entitäten.
 */
class TimeLineSettingsService : CrudService<TimeLineSettings> {

    private val storage: TimeLineSettingsStorage = TimeLineSettingsStorage()
    /**
     * Gibt alle vorhandenen Timeline-Einstellungen zurück.
     *
     * @return Liste aller Timeline-Einstellungen.
     */
    override fun findAll(): List<TimeLineSettings> {
        return storage.loadEntities().first // Lade alle TimelineSettings aus der Datei
    }

    /**
     * Findet eine spezifische Timeline-Einstellung anhand ihrer Benutzer-ID.
     *
     * @param id Benutzer-ID.
     * @return Eine Einstellung, wenn gefunden, ansonsten null.
     */
    override fun findById(id: Int): TimeLineSettings? {
        return findAll().find { it.userId == id }
    }

    /**
     * Speichert eine neue Timeline-Einstellung oder aktualisiert eine bestehende, falls diese existiert.
     *
     * @param entity Timeline-Einstellung, die gespeichert oder aktualisiert werden soll.
     */
    override fun save(entity: TimeLineSettings) {
        val entities = findAll().toMutableList()

        // Prüfen, ob bereits eine Timeline-Einstellung mit derselben userId existiert
        val existingIndex = entities.indexOfFirst { it.userId == entity.userId }
        if (existingIndex != -1) {
            // Ersetze vorhandene Einstellung
            entities[existingIndex] = entity
        } else {
            // Füge eine neue Einstellung hinzu
            entities.add(entity)
        }

        storage.saveEntities(entities) // Aktualisiere die Datei
    }

    /**
     * Löscht eine Timeline-Einstellung anhand ihrer Benutzer-ID.
     *
     * @param id Benutzer-ID der Einstellung, die gelöscht werden soll.
     */
    override fun delete(id: Int) {
        val entities = findAll().filter { it.userId != id } // Entferne die passende Einstellung
        storage.saveEntities(entities) // Speichere die aktualisierte Liste
    }
}