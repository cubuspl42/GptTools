package io.github.cubuspl42.gpttools

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import java.nio.file.Path
import kotlin.io.path.readText

data class GptAttachment(
    val name: String,
    val content: String,
) {
    val rawMessage: ChatMessage
        get() = ChatMessage(
            role = ChatRole.User,
            content = listOf(
                name,
                "----",
                content,
            ).joinToString(
                separator = "\n",
            ),
        )
}
