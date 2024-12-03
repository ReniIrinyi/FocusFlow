import java.io.File

fun main() {
    val sourceDir = File("src") // Source directory
    val outputDir = File("docs") // Documentation directory
    val plantUmlJar = File("C:\\Users\\renii\\Documents\\PROGRAMOZAS\\java\\plantuml.jar")
    val graphvizDotPath = "C:\\Program Files\\Graphviz\\bin\\dot.exe"

    if (!sourceDir.exists() || sourceDir.listFiles()?.isEmpty() == true) {
        println("Source code not found in '${sourceDir.absolutePath}' directory!")
        return
    }

    prepareOutputDirectory(outputDir)

    // Generate UML diagram
    generateUmlDiagram(sourceDir, outputDir, plantUmlJar, graphvizDotPath)

    // Generate documentation
    generateDocumentation(sourceDir, outputDir)

    println("All files have been successfully created in the '${outputDir.absolutePath}' directory!")
}

// UML diagram generation
fun generateUmlDiagram(sourceDir: File, outputDir: File, plantUmlJar: File, graphvizDotPath: String) {
    println("Generating UML diagram...")
    val umlContent = createUmlContent(sourceDir)
    val umlFile = File(outputDir, "diagram.puml")
    umlFile.writeText(umlContent)

    try {
        val process = ProcessBuilder(
            "java", "-DPLANTUML_DOT=$graphvizDotPath", "-jar", plantUmlJar.absolutePath, umlFile.absolutePath
        ).directory(outputDir).start()
        process.waitFor()

        val diagramFile = File(outputDir, "diagram.png")
        if (diagramFile.exists()) {
            println("UML diagram created: ${diagramFile.absolutePath}")
        } else {
            println("An error occurred while creating the UML diagram.")
        }
    } catch (e: Exception) {
        println("An error occurred while running PlantUML: ${e.message}")
    }
}

// List of primitive types
val primitiveTypes = setOf(
    "Int", "String", "Boolean", "Double", "Float", "Long", "Short", "Byte", "Char",
    "Unit", "Any", "Nothing", "List", "Set", "Map", "Array", "LocalDateTime", "LocalDate"
)

fun createUmlContent(sourceDir: File): String {
    val umlContent = StringBuilder()
    umlContent.append("@startuml\n")

    val classNames = mutableSetOf<String>() // Store class names
    val inheritanceRelationships = mutableSetOf<Pair<String, String>>() // Inheritance relationships
    val otherRelationships = mutableSetOf<String>() // Other relationships

    val classDeclarationRegex = Regex(
        """^\s*(?:public|private|protected|internal)?\s*(?:abstract\s+)?(class|data\s+class|object|interface)\s+(\w+)(?:\s*:\s*([^{]+))?\s*\{?""",
        RegexOption.MULTILINE
    )

    sourceDir.walkTopDown()
        .filter { it.extension == "kt" && !it.shouldBeExcluded() }
        .forEach { file ->
            val fileContent = file.readText()
            val classMatches = classDeclarationRegex.findAll(fileContent)
            for (classMatch in classMatches) {
                val classType = classMatch.groupValues[1]
                val className = classMatch.groupValues[2]
                val inheritanceList = classMatch.groupValues[3]
                val classBody = extractClassBody(fileContent.substring(classMatch.range.first))

                if (!classNames.contains(className)) {
                    umlContent.append("$classType $className {\n")
                    classNames.add(className)
                }

                // Process inheritance and interfaces
                if (!inheritanceList.isNullOrEmpty()) {
                    val parents = inheritanceList.split(",").map { it.trim().split("<")[0].split("(")[0].trim() }
                    parents.forEach { parentClassName ->
                        if (parentClassName.isNotEmpty()) {
                            inheritanceRelationships.add(parentClassName to className)
                            if (!classNames.contains(parentClassName)) {
                                classNames.add(parentClassName)
                                umlContent.append("class $parentClassName\n")
                            }
                        }
                    }
                }

                // Extract properties
                val propertyMatches = propertyPattern.findAll(classBody)
                for (propertyMatch in propertyMatches) {
                    val propertyName = propertyMatch.groupValues[2]
                    val propertyType = propertyMatch.groupValues[3]?.trim() ?: "Any"
                    umlContent.append("    + $propertyName: $propertyType\n")

                    // Add relationships based on property type
                    val typeNames = extractTypeNames(propertyType)
                    typeNames.forEach { typeName ->
                        if (!primitiveTypes.contains(typeName) && typeName != className) {
                            otherRelationships.add("$className --> $typeName")
                            if (!classNames.contains(typeName)) {
                                classNames.add(typeName)
                                umlContent.append("class $typeName\n")
                            }
                        }
                    }
                }

                // Extract functions
                val functionMatches = functionPattern.findAll(classBody)
                for (functionMatch in functionMatches) {
                    val methodName = functionMatch.groupValues[1]
                    val parameters = functionMatch.groupValues[2]
                    val returnType = functionMatch.groupValues[3]?.trim() ?: "Unit"
                    umlContent.append("    + $methodName(${parameters.replace("\n", " ")}): $returnType\n")

                    // Process parameter types
                    parameters.split(",").forEach { param ->
                        val parts = param.split(":").map { it.trim() }
                        if (parts.size == 2) {
                            val paramType = parts[1]
                            val paramTypeNames = extractTypeNames(paramType)
                            paramTypeNames.forEach { typeName ->
                                if (!primitiveTypes.contains(typeName) && typeName != className) {
                                    otherRelationships.add("$className --> $typeName")
                                    if (!classNames.contains(typeName)) {
                                        classNames.add(typeName)
                                        umlContent.append("class $typeName\n")
                                    }
                                }
                            }
                        }
                    }

                    // Process return type
                    val returnTypeNames = extractTypeNames(returnType)
                    returnTypeNames.forEach { typeName ->
                        if (!primitiveTypes.contains(typeName) && typeName != className) {
                            otherRelationships.add("$className --> $typeName")
                            if (!classNames.contains(typeName)) {
                                classNames.add(typeName)
                                umlContent.append("class $typeName\n")
                            }
                        }
                    }
                }

                umlContent.append("}\n")
            }
        }

    // Process inheritance relationships
    inheritanceRelationships.forEach { (parent, child) ->
        umlContent.append("$parent <|-- $child\n")
    }

    // Process other relationships
    otherRelationships.forEach { relationship ->
        umlContent.append("$relationship\n")
    }

    umlContent.append("@enduml\n")
    return umlContent.toString()
}

// Function to extract the class body by tracking braces
fun extractClassBody(classContent: String): String {
    var braceCount = 0
    val bodyBuilder = StringBuilder()
    var startRecording = false

    for (i in classContent.indices) {
        val c = classContent[i]
        if (c == '{') {
            braceCount++
            if (!startRecording) {
                startRecording = true
                continue
            }
        }
        if (c == '}') {
            braceCount--
        }
        if (startRecording) {
            if (braceCount == 0) {
                break
            }
            bodyBuilder.append(c)
        }
    }
    return bodyBuilder.toString()
}

// Adjusted property and function patterns
val propertyPattern = Regex(
    """(?m)^\s*(?:@\w+(?:\([^\)]*\))?\s*)*(?:public|private|protected|internal)?\s*(?:override\s+)?(?:lateinit\s+)?(val|var)\s+(\w+)(?:\s*:\s*([^=;{]+))?"""
)

val functionPattern = Regex(
    """(?m)^\s*(?:@\w+(?:\([^\)]*\))?\s*)*(?:public|private|protected|internal)?\s*(?:override\s+)?(?:suspend\s+)?(?:inline\s+)?fun\s+(\w+)\s*\((.*?)\)\s*(?::\s*([\w<>,? ]+))?"""
)

// Function to extract type names from type declarations
fun extractTypeNames(typeString: String): Set<String> {
    val typeNames = mutableSetOf<String>()

    // Remove nullability and whitespace
    val cleanType = typeString.replace("?", "").replace("\\s+".toRegex(), "")

    // Regex to match type names and generic parameters
    val regex = Regex("""([A-Za-z0-9_\.]+)(?:<(.+)>)?""")
    val matchResult = regex.matchEntire(cleanType)
    if (matchResult != null) {
        val baseType = matchResult.groupValues[1]
        typeNames.add(baseType)
        val genericTypes = matchResult.groupValues[2]
        if (genericTypes.isNotEmpty()) {
            // Split generic types by commas, handling nested generics
            var depth = 0
            val buffer = StringBuilder()
            for (c in genericTypes) {
                when (c) {
                    '<' -> {
                        depth++
                        buffer.append(c)
                    }
                    '>' -> {
                        depth--
                        buffer.append(c)
                    }
                    ',' -> if (depth == 0) {
                        typeNames.addAll(extractTypeNames(buffer.toString()))
                        buffer.clear()
                    } else {
                        buffer.append(c)
                    }
                    else -> buffer.append(c)
                }
            }
            if (buffer.isNotEmpty()) {
                typeNames.addAll(extractTypeNames(buffer.toString()))
            }
        }
    } else {
        // Handle simple types
        if (cleanType.isNotEmpty()) {
            typeNames.add(cleanType)
        }
    }
    return typeNames
}

// Adjusted function to include 'FileHandler' from 'utils' directory
fun File.shouldBeExcluded(): Boolean {
    val normalizedPath = this.path.replace("\\", "/").lowercase()
    // Exclude 'utils' and 'model' directories except for 'FileHandler.kt'
    val isExcludedDir = ((normalizedPath.contains("/utils/") || normalizedPath.contains("/model/")) &&
            !normalizedPath.endsWith("/filehandler"))
    // Adjusted to not exclude 'FileHandler.kt' in 'utils' directory
    return isExcludedDir
}

// Documentation generation remains the same
fun generateDocumentation(sourceDir: File, outputDir: File) {
    println("Generating documentation...")
    val links = mutableListOf<String>()

    sourceDir.walkTopDown()
        .filter { it.extension == "kt" && !it.shouldBeExcluded() }
        .forEach { file ->
            val docFile = File(outputDir, file.nameWithoutExtension + ".md")
            val docContent = extractDocumentation(file)

            docFile.writeText("""
# Documentation: ${file.nameWithoutExtension}

$docContent

---
""".trimIndent())

            links.add("""<li><a href="${file.nameWithoutExtension}.md">${file.nameWithoutExtension}</a></li>""")
        }

    val indexFile = File(outputDir, "index.html")
    indexFile.writeText("""
<!DOCTYPE html>
<html lang="hu">
<head>
    <meta charset="UTF-8">
    <title>Documentation</title>
</head>
<body>
    <h1>Project Documentation</h1>
    <h2>UML Diagram</h2>
    <img src="diagram.png" alt="UML Diagram">
    <h2>Source File Documentation</h2>
    <ul>
        ${links.joinToString("\n")}
    </ul>
</body>
</html>
""".trimIndent())

    println("HTML documentation generation complete: ${indexFile.absolutePath}")
}

// Extract documentation from source files
fun extractDocumentation(file: File): String {
    return file.readText()
        .split("/**").drop(1)
        .joinToString("\n") { block ->
            block.substringBefore("*/").lines()
                .joinToString("\n") { it.trim().removePrefix("*").trim() }
        }
}

// Prepare output directory
fun prepareOutputDirectory(outputDir: File) {
    if (outputDir.exists()) outputDir.deleteRecursively()
    outputDir.mkdir()
}
