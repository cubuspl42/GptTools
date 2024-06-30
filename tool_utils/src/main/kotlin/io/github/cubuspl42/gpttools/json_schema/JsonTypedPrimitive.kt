package io.github.cubuspl42.gpttools.json_schema

import kotlinx.serialization.json.JsonPrimitive

sealed interface JsonTypedPrimitive<PrimitiveTypeT : JsonPrimitiveType> {
    val jsonPrimitive: JsonPrimitive
}

data class JsonStringPrimitive(
    val value: String,
) : JsonTypedPrimitive<JsonStringType> {
    override val jsonPrimitive: JsonPrimitive
        get() = JsonPrimitive(value = value)
}

data class JsonNumberPrimitive(
    val value: Number,
) : JsonTypedPrimitive<JsonNumberType> {
    override val jsonPrimitive: JsonPrimitive
        get() = JsonPrimitive(value = value)
}

data class JsonIntegerPrimitive(
    val value: Int,
) : JsonTypedPrimitive<JsonIntegerType> {
    override val jsonPrimitive: JsonPrimitive
        get() = JsonPrimitive(value = value)
}
