package ru.mit.spbau.sd.chat.client

import ru.spbau.mit.sd.commons.proto.ChatMessage

/**
 * Simply builds protobuf instance of `ChatMessage` from text
 */
fun createChatMessage(text: String): ChatMessage {
    return ChatMessage.newBuilder()
            .setTimestamp(System.currentTimeMillis()).setText(text).build()!!
}
