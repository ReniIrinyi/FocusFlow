import java.io.File

fun main() {
    val sourceDir = File("src") // Quellcode-Verzeichnis
    val outputDir = File("docs") // Zielverzeichnis für die generierte Dokumentation

    // Prüfen, ob das Quellcode-Verzeichnis existiert und Dateien enthält
    if (!sourceDir.exists() || sourceDir.listFiles()?.isEmpty() == true) {
        println("Keine Quelldateien im Verzeichnis ${sourceDir.absolutePath} gefunden.")
        return
    }

    // Das 'docs'-Verzeichnis bereinigen oder erstellen
    if (outputDir.exists()) {
        outputDir.deleteRecursively() // Löscht vorhandene Dateien im 'docs'-Verzeichnis
    }
    outputDir.mkdir() // Erstellt das Verzeichnis

    println("Generiere Dokumentation...")

    val baseDocumentation = """
<h1>Projektübersicht: FocusFlow</h1>
<p><b>Projektziel:</b> Dieses Projekt ist eine einfache Aufgabenverwaltungs-App, die es Benutzern ermöglicht, Aufgaben zu erstellen, zu bearbeiten, zu löschen und Prioritäten zu verwalten.</p>
<p><b>Technologien:</b> Kotlin, Java</p>
<p><b>Hauptkomponenten:</b></p>
<ul>
    <li><code>TaskService.kt</code>: Verarbeitet die Geschäftslogik der Aufgabenverwaltung.</li>
    <li><code>MainMenu.kt</code>: Die Hauptschnittstelle für den Benutzer, um das Programm zu bedienen.</li>
</ul>
<p><b>Funktionen:</b></p>
<ul>
    <li>Aufgaben hinzufügen, bearbeiten und löschen.</li>
    <li>Prioritäten verwalten (hoch, mittel, niedrig).</li>
    <li>Suche und Filter nach Aufgabenstatus.</li>
    <li>Automatische Dokumentationsgenerierung (dieses Skript).</li>
</ul>
<p><b>Ausführung:</b> Clone das Repository, und führe <code>GenerateDocs.kt</code> file um die Dokumentation zu erstellen, oder starte die Applikation aus der <code>Main.kt</code>.</p>
"""

    // Funktion zum Lesen des ersten KDoc-Kommentars im Dateikopf
    fun getFileDescription(lines: List<String>): String {
        val kdoc = lines.takeWhile { it.trim().startsWith("/**") || it.trim().startsWith("*") || it.trim().endsWith("*/") }
        return kdoc
            .joinToString("\n") {
                it.trim()
                    .removePrefix("/**")
                    .removePrefix("*")
                    .removeSuffix("*/")
                    .trim()
                    .removeSuffix("/")
            }
            .trim()
    }

    // Funktion zur Erstellung des Inhalts für eine Datei
    fun processFile(file: File, alwaysOpen: Boolean = false): String {
        val fileLines = file.readLines()
        val description = getFileDescription(fileLines) // Beschreibung des Datei-Kopfs
        val content = StringBuilder()

        // Ha a fájl Main.kt, akkor mindig nyitott
        val openTag = if (alwaysOpen) " open" else ""

        content.append("<li><details$openTag><summary>${file.nameWithoutExtension}</summary>\n")

        // Beschreibung hinzufügen, wenn verfügbar
        if (description.isNotBlank()) {
            content.append("<p>$description</p>\n")
        }

        val functions = StringBuilder()
        var currentComment = mutableListOf<String>()
        var lastFunctionName: String? = null

        for (line in fileLines) {
            val trimmed = line.trim()

            // KDoc-Kommentare lesen
            if (trimmed.startsWith("/**") || trimmed.startsWith("*") || trimmed.endsWith("*/")) {
                currentComment.add(
                    trimmed
                        .removePrefix("/**")
                        .removePrefix("*")
                        .removeSuffix("*/")
                        .trim()
                        .removeSuffix("/")
                )
            } else if (trimmed.startsWith("fun ")) {
                // A függvény neve és paraméterei
                lastFunctionName = trimmed.substringAfter("fun ").substringBefore("{").trim()

                if (currentComment.isNotEmpty()) {
                    functions.append("<h4>Funktion: $lastFunctionName</h4>\n")
                    functions.append("<p>${currentComment.joinToString("<br>") { it.trim() }}</p>\n")
                    currentComment.clear()
                }
            }
        }

        if (functions.isNotEmpty()) {
            content.append("<div class='content'>\n")
            content.append(functions.toString())
            content.append("</div>\n")
        }

        content.append("</details></li>\n")
        return content.toString()
    }

    // Rekursive Verarbeitung der Dateien und Ordner
    fun processDirectory(dir: File, isUtilsFolder: Boolean = false): String {
        val subLinks = dir.listFiles()?.sorted()?.filter { it.name != "GenerateDocs.kt" }?.joinToString("\n") { file ->
            if (file.isDirectory) {
                val isUtils = file.name == "utils" // Prüfen, ob es der utils-Ordner ist
                if (isUtils) {
                    // Utils-Dateien werden speziell behandelt
                    "<li><details open><summary><b>${file.name}</b></summary><ul>\n${processDirectory(file, isUtilsFolder = true)}</ul></details></li>"
                } else {
                    "<li><details><summary><b>${file.name}</b></summary><ul>\n${processDirectory(file)}\n</ul></details></li>"
                }
            } else if (file.extension == "kt") {
                val isMain = file.name == "Main.kt"
                processFile(file, alwaysOpen = isMain) // A Main.kt mindig nyitva
            } else ""
        } ?: ""

        return subLinks
    }

    // Startpunkt: Verarbeitung der Verzeichnisse und Dateien
    val htmlContent = processDirectory(sourceDir)

    // Index-HTML erstellen
    val indexFile = File(outputDir, "index.html")
    indexFile.writeText("""
<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dokumentationsübersicht</title>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; }
        h4 {margin-bottom:0}
        p {margin-top:0}
        ul { list-style-type: none; padding-left: 20px; }
        li { margin-bottom: 10px; }
        details summary { cursor: pointer; font-weight: bold; }
        .content { margin-left: 20px; }
    </style>
</head>
<body>
    $baseDocumentation
    <h2>Codestruktur</h2>
    <ul>
        $htmlContent
    </ul>
</body>
</html>
""".trimIndent())

    println("Dokumentation wurde im Verzeichnis ${outputDir.absolutePath} erstellt.")
}
