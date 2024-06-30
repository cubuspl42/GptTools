package io.github.cubuspl42.gpttools

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.Tool
import com.aallam.openai.api.chat.ToolCall
import com.aallam.openai.api.chat.ToolChoice
import com.aallam.openai.api.core.Parameters
import com.aallam.openai.api.core.Usage
import com.aallam.openai.client.OpenAI

class GptUtilClient private constructor(
    private val gptModelId: GptModelId = GptModelId.Gpt4o,
    private val openAiToken: String,
) {
    companion object {
        fun createFromToken(
            openAiToken: String,
        ): GptUtilClient = GptUtilClient(
            openAiToken = openAiToken,
        )

        fun createFromEnv(): GptUtilClient {
            val token = System.getenv("OPENAI_API_KEY") ?: throw IllegalStateException("OPENAI_API_KEY is not set")

            return createFromToken(openAiToken = token)
        }
    }

    private val rawOpenAiClient = OpenAI(
        token = openAiToken,
    )

    suspend fun <ResultT> executeFunction(
        systemPrompt: String,
        attachmentSets: List<GptAttachmentSet>,
        taskDescription: String,
        function: GptFunction<ResultT>,
    ): ResultT {
        val rawTool = Tool.function(
            name = function.name,
            description = function.description,
            parameters = Parameters(
                schema = function.argumentSchema.json,
            ),
        )

        val messages = listOf(
            ChatMessage(
                role = ChatRole.System,
                content = systemPrompt,
            ),
        ) + attachmentSets.flatMap {
            it.rawMessages
        } + listOf(
            ChatMessage(
                role = ChatRole.User,
                content = taskDescription,
            ),
        )

        messages.forEach {
            println("${it.role}:")
            println(it.content)
        }

        val rawResponse = rawOpenAiClient.chatCompletion(
            request = ChatCompletionRequest(
                model = gptModelId.rawModelId,
                tools = listOf(
                    rawTool,
                ),
                toolChoice = ToolChoice.function(
                    name = function.name,
                ),
                messages = messages,
            ),
        )

        val usage = rawResponse.usage ?: throw IllegalStateException("Usage is not present")

        println(usage)

        val cost = usage.calculateCost()
        println(cost)
        println("Total cost: ${cost.total}")

        val rawResponseChoice = rawResponse.choices.singleOrNull()
            ?: throw IllegalStateException("Unexpected response choice count: ${rawResponse.choices.size}")

        val rawResponseMessage = rawResponseChoice.message

        if (rawResponseMessage.role != ChatRole.Assistant) {
            throw IllegalStateException("Unexpected response role: ${rawResponseMessage.role}")
        }

        val rawToolCalls = rawResponseMessage.toolCalls ?: throw IllegalStateException("Tool calls are not present")

        val rawTooCall = rawToolCalls.singleOrNull()
            ?: throw IllegalStateException("Unexpected tool call count: ${rawToolCalls.size}")

        val rawToolFunctionCall = rawTooCall as? ToolCall.Function
            ?: throw IllegalStateException("Unexpected tool call type: ${rawTooCall::class}")

        val rawFunctionCall = rawToolFunctionCall.function

        if (rawFunctionCall.name != function.name) {
            throw IllegalStateException("Unexpected tool call name: ${rawFunctionCall.name}")
        }

        val argument = rawFunctionCall.argumentsAsJson()

        return function.execute(argument = argument)
    }
}

data class Cost(
    /**
     * Cost of input tokens in USD.
     */
    val input: Double,
    /**
     * Cost of output tokens in USD.
     */
    val output: Double,
) {
    /**
     * Total cost in USD.
     */
    val total: Double
        get() = input + output
}

// $5.00 / 1M input tokens
private const val inputTokenPrice = 5.0 / 1_000_000.0

// $15.00 / 1M output tokens
private const val outputTokenPrice = 15.0 / 1_000_000.0

private fun Usage.calculateCost(): Cost {
    val inputCost = (promptTokens ?: 0) * inputTokenPrice
    val outputCost = (completionTokens ?: 0) * outputTokenPrice

    return Cost(
        input = inputCost,
        output = outputCost,
    )
}
