package io.github.cubuspl42.gpttools

import io.github.cubuspl42.gpttools.json_schema.JsonSchemaElement
import kotlinx.serialization.json.JsonElement

abstract class GptFunction<ResultT> {
    abstract val name: String

    abstract val description: String

    abstract val argumentSchema: JsonSchemaElement

    abstract fun execute(
        argument: JsonElement,
    ): ResultT
}
