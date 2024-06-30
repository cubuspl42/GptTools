package io.github.cubuspl42.gpttools

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole

data class GptAttachmentSet(
    /**
     * Should end with ":", "...", or alike.
     */
    val description: String,
    val attachments: List<GptAttachment>,
) {
    val rawMessages: List<ChatMessage>
        get() = listOf(
            ChatMessage(
                role = ChatRole.User,
                content = description,
            )
        ) + attachments.map {
            it.rawMessage
        }
}
