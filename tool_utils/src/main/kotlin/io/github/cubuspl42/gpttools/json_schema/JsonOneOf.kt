package io.github.cubuspl42.gpttools.json_schema

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

data class JsonOneOf(
    override val description: String?,
    val options: List<JsonSchemaElement>,
) : JsonSchemaElement() {
    override val jsonElementSpecificContent: Map<String, JsonElement>
        get() = mapOf(
            "oneOf" to JsonArray(
                content = options.map { it.json },
            ),
        )
}
