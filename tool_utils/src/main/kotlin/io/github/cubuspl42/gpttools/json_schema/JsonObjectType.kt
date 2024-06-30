package io.github.cubuspl42.gpttools.json_schema

import io.github.cubuspl42.gpttools.mapOfNotNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

data class JsonObjectType(
    override val description: String? = null,
    override val nullable: Boolean = false,
    val properties: Map<String, Property>,
) : JsonType() {
    companion object {
        fun of(
            description: String? = null,
            nullable: Boolean = false,
            properties: Map<String, JsonSchemaElement>,
        ): JsonObjectType = JsonObjectType(
            description = description,
            nullable = nullable,
            properties = properties.mapValues { (_, element) ->
                Property(
                    required = true,
                    element = element,
                )
            },
        )
    }

    data class Property(
        val required: Boolean,
        val element: JsonSchemaElement,
    )

    override val name: String = "object"

    override val jsonTypeSpecificContent: Map<String, JsonElement>
        get() {
            val requiredKeys = properties.mapNotNull { (key, property) ->
                when {
                    property.required -> JsonPrimitive(value = key)
                    else -> null
                }
            }

            return mapOfNotNull(
                "properties" to JsonObject(
                    content = properties.mapValues { (_, value) -> value.element.json },
                ),
                when {
                    requiredKeys.isEmpty() -> null
                    else -> "required" to JsonArray(content = requiredKeys)
                },
            )
        }
}
