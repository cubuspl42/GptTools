package io.github.cubuspl42.gpttools.json_schema

import io.github.cubuspl42.gpttools.mapOfNotNull
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

data class JsonArrayType(
    override val description: String? = null,
    override val nullable: Boolean = false,
    val itemType: JsonType,
    val maxItems: Int? = null,
) : JsonType() {
    override val name: String = "array"

    override val jsonTypeSpecificContent: Map<String, JsonElement>
        get() = mapOfNotNull(
            "items" to itemType.json,
            maxItems?.let { "maxItems" to JsonPrimitive(value = it) },
        )
}
