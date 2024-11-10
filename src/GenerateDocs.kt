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
        outputDir.deleteRecursively()
    }
    outputDir.mkdir()

    println("Generiere Dokumentation...")

    fun getFileDescription(lines: List<String>): String {
        val kdoc = lines.takeWhile { it.trim().startsWith("/**") || it.trim().startsWith("*") }
        return kdoc.joinToString("\n") { it.trim().removePrefix("*").trim() }
    }

    fun processFile(file: File): String {
        val fileLines = file.readLines()
        val description = getFileDescription(fileLines) // Beschreibung des Datei-Kopfs
        val content = StringBuilder()

        // Markdown-Dokumentation erstellen
        content.append("<h3>${file.nameWithoutExtension}</h3>\n")
        content.append("<p style='margin-bottom: 5px;'>${description}</p>\n") // Kevesebb hely a gomb előtt
        content.append("<div class='content' style='display: none;'>\n")

        var currentComment = mutableListOf<String>()
        var lastFunctionName: String? = null

        for (line in fileLines) {
            val trimmed = line.trim()

            // KDoc-Kommentare lesen
            if (trimmed.startsWith("/**") || trimmed.startsWith("*")) {
                currentComment.add(trimmed.removePrefix("*").trim())
            } else if (trimmed.startsWith("fun ")) {
                lastFunctionName = trimmed.substringAfter("fun ").substringBefore("(").trim()

                if (currentComment.isNotEmpty()) {
                    content.append("<h4>Funktion: $lastFunctionName</h4>\n")
                    content.append("<p>${currentComment.joinToString("<br>") { it.trim() }}</p>\n")
                    currentComment.clear()
                }
            }
        }

        content.append("</div>\n")
        content.append("<button class='toggle-btn' style='margin-bottom: 15px;'>Details anzeigen</button>\n") // Nagyobb hely a gomb után

        return content.toString()
    }

    fun processDirectory(dir: File): String {
        val subLinks = dir.listFiles()?.sorted()?.filter { it.name != "GenerateDocs.kt" }?.joinToString("\n") { file ->
            if (file.isDirectory) {
                "<details><summary><b>${file.name}</b></summary>\n${processDirectory(file)}\n</details>"
            } else if (file.extension == "kt") {
                processFile(file)
            } else ""
        } ?: ""

        return "<ul>\n$subLinks\n</ul>"
    }

    // Startpunkt: Verarbeitung der Verzeichnisse und Dateien
    val htmlContent = processDirectory(sourceDir)

    // Index-HTML mit eingebettetem JavaScript und CSS erstellen
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
        .content { margin-left: 20px; }
        .toggle-btn { margin-top: 5px; margin-bottom: 15px; cursor: pointer; background-color: #007BFF; color: white; border: none; padding: 5px 10px; border-radius: 5px; }
    </style>
</head>
<body>
    <h1>Dokumentationsübersicht</h1>
    $htmlContent
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            document.querySelectorAll('.toggle-btn').forEach(button => {
                button.addEventListener('click', () => {
                    const content = button.previousElementSibling;
                    if (content.style.display === 'none') {
                        content.style.display = 'block';
                        button.textContent = 'Details ausblenden';
                    } else {
                        content.style.display = 'none';
                        button.textContent = 'Details anzeigen';
                    }
                });
            });
        });
    </script>
</body>
</html>
""".trimIndent())

    println("Dokumentation wurde im Verzeichnis ${outputDir.absolutePath} erstellt.")
}
