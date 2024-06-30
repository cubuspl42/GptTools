import io.github.cubuspl42.gpttools.GptAttachment
import io.github.cubuspl42.gpttools.GptAttachmentSet
import io.github.cubuspl42.gpttools.GptFunction
import io.github.cubuspl42.gpttools.GptUtilClient
import io.github.cubuspl42.gpttools.executeTree
import io.github.cubuspl42.gpttools.findFilesRecursively
import io.github.cubuspl42.gpttools.json_schema.JsonArrayType
import io.github.cubuspl42.gpttools.json_schema.JsonObjectType
import io.github.cubuspl42.gpttools.json_schema.JsonStringType
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class AiTasksTool(
    private val projectPath: Path,
    private val gptUtilClient: GptUtilClient,
) {
    data class WriteSourceFileCommand(
        val path: Path,
        val content: String,
    ) {
        companion object {
            fun loadFromJsonElement(
                jsonElement: JsonElement,
            ): WriteSourceFileCommand {
                val jsonObject = jsonElement.jsonObject

                return WriteSourceFileCommand(
                    path = Path.of(jsonObject.getValue("path").jsonPrimitive.content),
                    content = jsonObject.getValue("content").jsonPrimitive.content,
                )
            }
        }

        init {
            require(!path.isAbsolute)
            require(path == path.normalize())
        }

        fun execute(absoluteRootPath: Path) {
            require(absoluteRootPath.isAbsolute)

            val absoluteFilePath = absoluteRootPath.resolve(path)

            absoluteFilePath.createParentDirectories()

            absoluteFilePath.writeText(content)
        }
    }

    init {
        require(projectPath.isAbsolute)
    }

    private fun loadFileAttachmentSet(
        description: String,
        absolutePaths: List<Path>,
    ): GptAttachmentSet = GptAttachmentSet(
        description = description,
        attachments = absolutePaths.map { absolutePath ->
            val relativePath = projectPath.relativize(absolutePath)

            GptAttachment(
                name = relativePath.toString(),
                content = absolutePath.readText(),
            )
        },
    )

    private fun loadTreeAttachmentSet(): GptAttachmentSet = GptAttachmentSet(
        description = "Main source root:",
        attachments = listOf(
            GptAttachment(
                name = "tree",
                content = executeTree(
                    path = projectPath.resolve(Path.of("src/main/kotlin")),
                ),
            ),
        ),
    )

    /**
     * Finds all files related to the given topic. Follows the topic dependency chain.
     */
    private fun findFilesSomehowRelatedToTopic(
        originalTopicName: String,
    ): Set<Path> {
        val directlyRelatedFilePaths = findFilesDirectlyRelatedToTopic(topicName = originalTopicName)

        return directlyRelatedFilePaths + directlyRelatedFilePaths.flatMap { filePath ->
            findSomehowRelatedFiles(originalFilePath = filePath)
        }
    }

    /**
     * Finds all files somehow related to the given file. Follows the topic dependency chain.
     */
    private fun findSomehowRelatedFiles(
        originalFilePath: Path,
    ): Set<Path> {
        val dependencyTopics = extractDependencyTopics(source = originalFilePath.readText())

        return dependencyTopics.flatMap {
            findFilesSomehowRelatedToTopic(originalTopicName = it)
        }.toSet()
    }

    /**
     * Finds all files somehow related to the given set of files. Follows the topic dependency chain.
     */
    private fun findSomehowRelatedFiles(
        originalFilePaths: Set<Path>,
    ): Set<Path> = originalFilePaths.flatMap { filePath ->
        findSomehowRelatedFiles(originalFilePath = filePath)
    }.toSet()

    /**
     * Finds all files directly related to the given topic. Does not follow the topic dependency chain.
     */
    private fun findFilesDirectlyRelatedToTopic(
        topicName: String,
    ): Set<Path> = findFilesRecursively(
        rootDirectoryPath = projectPath,
        pattern = "RelatedTo(Topic.$topicName)",
    ).toSet()

    suspend fun run() {
        val referenceFilePaths = findFilesRecursively(
            rootDirectoryPath = projectPath,
            pattern = "AI.do",
        )

        val taskFilePaths = findFilesRecursively(
            rootDirectoryPath = projectPath,
            pattern = "AI.do",
        )

        val relatedFilePaths = findSomehowRelatedFiles(
            originalFilePaths = referenceFilePaths.toSet() + taskFilePaths.toSet(),
        )

        val relatedFilesAttachmentSet = loadFileAttachmentSet(
            description = "Files provided for reference:",
            absolutePaths = relatedFilePaths.toList(),
        )

        val taskFilesAttachmentSet = loadFileAttachmentSet(
            description = "Files with something to do:",
            absolutePaths = taskFilePaths.toList(),
        )

        gptUtilClient.executeFunction(
            systemPrompt = """
                You are an AI model specialized in generating production-ready source code. ALWAYS perform the task given by the user in full. NEVER provide code which is "just a starting point", comments like "// TODO", etc.
                
                You will be tipped $500 USD if the generated code is to the highest standards.
            """.trimIndent(),
            attachmentSets = listOf(
                loadTreeAttachmentSet(),
                relatedFilesAttachmentSet,
                taskFilesAttachmentSet,
            ),
            taskDescription = """
                Follow the code style. Prefer named arguments, add trailing commas. Don't add topic annotations on your own.
                
                Replace each AI.do comment with relevant code.
            """.trimIndent(),
            function = object : GptFunction<Unit>() {
                override val name: String = "write_source_files"

                override val description: String = "Write the source files to the filesystem"

                override val argumentSchema = JsonObjectType.of(
                    properties = mapOf(
                        "commands" to JsonArrayType(
                            itemType = JsonObjectType.of(
                                properties = mapOf(
                                    "path" to JsonStringType(
                                        description = "Relative path to the file",
                                    ),
                                    "content" to JsonStringType(
                                        description = "File content",
                                    ),
                                ),
                            ),
                        ),
                    ),
                )

                override fun execute(
                    argument: JsonElement,
                ) {
                    val commands = argument.jsonObject.getValue("commands").jsonArray.map {
                        WriteSourceFileCommand.loadFromJsonElement(it)
                    }

                    commands.forEach {
                        it.execute(absoluteRootPath = projectPath)
                    }
                }
            },
        )
    }
}

private val topicPattern = Regex("""@DependsOn\(Topic\.(\w+)\)""")

/**
 * Extracts all topics the given source depends on.
 *
 * The topic dependency is declared using this text pattern: `@DependsOn(Topic.$TOPIC_NAME)`
 */
private fun extractDependencyTopics(
    source: String,
): Set<String> = topicPattern.findAll(source).map {
    it.groupValues[1]
}.toSet()

suspend fun main() {
    val projectPath = System.getenv("PROJECT_PATH")

    val gptUtilClient = GptUtilClient.createFromEnv()

    val aiTasksTool = AiTasksTool(
        projectPath = Path.of(projectPath),
        gptUtilClient = gptUtilClient,
    )

    aiTasksTool.run()
}
