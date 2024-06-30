package io.github.cubuspl42.gpttools

import com.aallam.openai.api.model.ModelId as RawModelId

sealed class GptModelId {
    internal val rawModelId: RawModelId
        get() = RawModelId(id = id)

    abstract val id: String

    data object Gpt4o : GptModelId() {
        override val id = "gpt-4o"
    }
}
