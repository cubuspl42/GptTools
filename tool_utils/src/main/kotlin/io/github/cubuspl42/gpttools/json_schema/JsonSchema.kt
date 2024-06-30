package io.github.cubuspl42.gpttools.json_schema

import io.github.cubuspl42.gpttools.mapOfNotNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

sealed class JsonSchemaElement {
    val json: JsonObject
        get() = JsonObject(
            content = mapOfNotNull(
                description?.let { "description" to JsonPrimitive(value = it) },
            ) + jsonElementSpecificContent,
        )

    abstract val description: String?

    abstract val jsonElementSpecificContent: Map<String, JsonElement>
}
