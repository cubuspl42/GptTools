package io.github.cubuspl42.gpttools.json_schema

import kotlinx.serialization.json.JsonElement

sealed class JsonPrimitiveType : JsonType() {
    final override val jsonTypeSpecificContent: Map<String, JsonElement> = emptyMap()
}

data class JsonStringType(
    override val nullable: Boolean = false,
    override val description: String? = null,
) : JsonPrimitiveType() {
    override val name: String = "string"
}

data class JsonNumberType(
    override val nullable: Boolean = false,
    override val description: String? = null,
) : JsonPrimitiveType() {
    override val name: String = "number"
}

data class JsonIntegerType(
    override val nullable: Boolean = false,
    override val description: String? = null,
) : JsonPrimitiveType() {
    override val name: String = "integer"
}
