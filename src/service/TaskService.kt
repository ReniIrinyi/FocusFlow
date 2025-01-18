package service

import model.Task
import controller.TaskStorage
import utils.ImgHandler

/**
 * Die TaskService-Klasse dient der Verwaltung von Task-Entitäten (Aufgaben).
 * Diese Klasse implementiert das CrudService-Interface für den Typ "Task".
 */
class TaskService : CrudService<Task> {

    // Verbindung zur TaskStorage-Klasse, die die Speicherung der Entitäten verwaltet.
    private val storage = TaskStorage()

    /**
     * Ruft alle Task-Entitäten aus dem Speicher ab.
     *
     * @return Eine Liste aller gespeicherten Tasks.
     */
    override fun findAll(): List<Task> {
        return storage.loadEntities().first // Lädt alle Aufgaben (Tasks) und gibt die Liste zurück.
    }

    /**
     * Ruft eine spezifische Task anhand ihrer eindeutigen ID ab.
     *
     * @param id Die ID der gesuchten Task.
     * @return Die gefundene Task, falls vorhanden, ansonsten null.
     */
    override fun findById(id: Int): Task? {
        return findAll().find { it.id == id } // Sucht nach einer Task mit der angegebenen ID in der Liste.
    }

    fun findByUserId(userId: Int): List<Task> {
        val tasks = findAll()
        println(tasks.filter { it.userId == userId })
        return tasks.filter { it.userId == userId }
    }

    override fun save(entity: Task) {
        val tasks = findAll() // Ruft alle Aufgaben ab.
        val existingTask = tasks.find { it.id == entity.id } // Überprüft, ob die Aufgabe bereits existiert.

        if (existingTask == null) {
            // Wenn die Aufgabe nicht existiert, wird sie als neue hinzugefügt.
            storage.addEntity(entity)
        } else {
            // Falls die Aufgabe existiert, wird sie aktualisiert.
            storage.updateEntity(entity.id, entity)
        }
    }

    /**
     * Speichert eine neue Task oder aktualisiert eine bestehende.
     *
     * @param entity Die Task, die gespeichert oder aktualisiert werden soll.
     */
    fun encodeImageToBase64(absolutePath: String): String {
        val imgHandler = ImgHandler()
        return imgHandler.encodeImageToBase64(absolutePath)
    }

    /**
     * Löscht eine Task-Entität anhand ihrer ID.
     *
     * @param id Die ID der zu löschenden Task.
     */
    override fun delete(id: Int) {
        storage.deleteEntityById(id) // Löscht die Task anhand der ID.
    }


}