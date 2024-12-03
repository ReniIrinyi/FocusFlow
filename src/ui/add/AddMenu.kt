package ui.add

import model.Task
import service.TaskService
import java.util.*

/**
 * Diese Klasse verwaltet das Hinzufügen neuer Aufgaben.
 *    - Die Geschäftslogik wird durch den TaskService bereitgestellt.
 *
 * Zeigt das Menü zum Hinzufügen neuer Aufgaben an.
 * TODO @alex:
 * 1. Fordere den Benutzer auf, die folgenden Informationen einzugeben:
 *    - Titel der Aufgabe.
 *    - Priorität der Aufgabe (Hoch, Mittel, Niedrig).
 *    - Fälligkeitsdatum der Aufgabe (YYYY-MM-DD).
 * 2. Stelle sicher:
 *    - Keine Eingabe ist leer.
 *    - Die Priorität ist gültig (verwende `InputValidator.isValidPriority`).
 *    - Das Fälligkeitsdatum ist ein gültiges Datum (verwende `InputValidator.isValidDate`).
 * 3. Erstelle ein neues `Task`-Objekt mit den eingegebenen Informationen.
 * 4. Verwende `taskService.addTask()`, um die Aufgabe zur Liste hinzuzufügen.
 * 5. Gib dem Benutzer eine Bestätigung aus, dass die Aufgabe erfolgreich hinzugefügt wurde.
 */
class AddMenu(private val taskService: TaskService) {
}
