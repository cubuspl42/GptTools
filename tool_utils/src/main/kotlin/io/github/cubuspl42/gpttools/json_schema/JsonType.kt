package io.github.cubuspl42.gpttools.json_schema

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

sealed class JsonType : JsonSchemaElement() {
    abstract val name: String

    abstract val nullable: Boolean

    final override val jsonElementSpecificContent: Map<String, JsonElement>
        get() {
            val type = if (nullable) JsonArray(
                content = listOf(
                    JsonPrimitive(name),
                    JsonPrimitive("null"),
                ),
            ) else JsonPrimitive(name)

            return mapOf("type" to type) + jsonTypeSpecificContent
        }

    abstract val jsonTypeSpecificContent: Map<String, JsonElement>
}
