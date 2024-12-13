# Documentation: FileHandler


Diese Klasse verwaltet das Speichern, Laden und löschen von Aufgaben aus einer Datei.

Ziel:
- Persistente Speicherung der Aufgaben, auch nach Beenden der Anwendung.
- Verwendung eines benutzerdefinierten Textformats für die Speicherung.
- Unabhängigkeit von der Geschäftslogik (TaskService).

Funktionen:
- `loadTasks()`: Liest die Aufgaben aus der Datei und gibt sie als Liste zurück.
- `saveTasks(tasks)`: Speichert die übergebene Liste von Aufgaben in die Datei.

Hinweise:
- Wenn die Datei nicht existiert, wird sie automatisch erstellt.
- Falls die Datei leer ist, wird eine leere Liste zurückgegeben.


Lädt alle Aufgaben aus der Datei.
@return Eine Liste der gespeicherten Aufgaben (`List<Task>`) oder eine leere Liste bei Fehlern.


Speichert alle Aufgaben in der Datei.
@param tasks Die Liste der Aufgaben, die gespeichert werden sollen.
@return 200 bei Erfolg, 400 bei Fehler.


Löscht eine Aufgabe anhand der ID.
@param taskId Die ID der zu löschenden Aufgabe.
@return 200 bei Erfolg, 404 wenn keine Aufgabe mit der ID gefunden wurde, 400 bei anderen Fehlern.


Aktualisiert die Priorität einer Aufgabe basierend auf ihrer ID.

Schritte:
1. Validiert den neuen Prioritätswert.
2. Lädt die Aufgaben aus der Datei.
3. Sucht die Aufgabe mit der gegebenen ID.
- Wenn die Aufgabe gefunden wird: Aktualisiert die Priorität.
- Wenn die Aufgabe nicht gefunden wird: Gibt einen 404-Fehlercode zurück.
4. Speichert die aktualisierte Liste in die Datei.

@param taskId Die ID der Aufgabe, die aktualisiert werden soll.
@param newPriority Die neue Priorität für die Aufgabe.
@return 200 bei Erfolg, 404 wenn keine Aufgabe gefunden wurde, 400 bei ungültiger Priorität oder Fehler.


---