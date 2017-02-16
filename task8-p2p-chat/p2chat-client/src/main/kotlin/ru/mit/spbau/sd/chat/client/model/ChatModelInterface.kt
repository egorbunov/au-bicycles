package ru.mit.spbau.sd.chat.client.model

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo

/**
 * Provides modifications-safe interface to chat model
 */
class ChatModelInterface<T>(private val chatModel: ChatModel<T>) {

    /**
     * Get all chat users
     */
    fun getAllUsers(): List<Pair<T, ChatUserInfo>> {
        return chatModel.getUsers()
    }
}