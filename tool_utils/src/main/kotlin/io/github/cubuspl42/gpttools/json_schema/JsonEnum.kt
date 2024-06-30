package io.github.cubuspl42.gpttools.json_schema

import kotlinx.serialization.json.JsonArray

data class JsonEnum<PrimitiveTypeT : JsonPrimitiveType>(
    override val description: String?,
    val type: PrimitiveTypeT,
    val values: List<JsonTypedPrimitive<PrimitiveTypeT>>,
) : JsonSchemaElement() {
    override val jsonElementSpecificContent
        get() = type.jsonElementSpecificContent + mapOf(
            "enum" to JsonArray(
                content = values.map { it.jsonPrimitive },
            )
        )
}
