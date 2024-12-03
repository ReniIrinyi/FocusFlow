# Documentation: ShowMenu


Diese Klasse zeigt die Details einer Aufgabe an und ermöglicht die Bearbeitung (Edit-Modus).
- Die Geschäftslogik wird durch TaskService bereitgestellt.


Zeigt die Details der Aufgabe mit der übergebenen ID an.
TODO @david:
1. Verwende `taskService.getTask(taskId)`, um die Aufgabe abzurufen.
2. Wenn die Aufgabe gefunden wird:
- Zeige die Details der Aufgabe in der Konsole.
- Frage den Benutzer, ob die Aufgabe bearbeitet werden soll (y/n).
3. Wenn der Benutzer "y" wählt, wechsle in den Bearbeitungsmodus (`editTask`).
4. Wenn die Aufgabe nicht gefunden wird, gib eine Fehlermeldung aus.


Ermöglicht die Bearbeitung einer Aufgabe.
TODO @david:
1. Frage den Benutzer nacheinander nach:
- Neuem Titel (oder lasse den alten Titel unverändert, wenn nichts eingegeben wird).
- Neuer Priorität (Hoch, Mittel, Niedrig).
- Neuem Fälligkeitsdatum (YYYY-MM-DD).
2. Aktualisiere die Aufgabe mit den neuen Werten.
3. Verwende `taskService.updateTask(taskId, updatedTask)`, um die Änderungen zu speichern.
4. Gib dem Benutzer eine Bestätigung aus, dass die Aufgabe aktualisiert wurde.


---