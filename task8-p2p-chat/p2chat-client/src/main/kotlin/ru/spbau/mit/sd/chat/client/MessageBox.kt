package ru.spbau.mit.sd.chat.client

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.util.*


/**
 * Structure with all messages for particular client (that is one chat room)
 */
class MessageBox(val user: ChatUserInfo) {
    private val messages = ArrayList<Pair<ChatUserInfo, ChatMessage>>()

    fun addMessage(from: ChatUserInfo, message: ChatMessage) {
        messages.add(Pair(from, message))
    }

    fun getLastMessages(num: Int): List<Pair<ChatUserInfo, ChatMessage>> {
        return messages.slice(messages.lastIndex-num..messages.lastIndex)
    }

    fun getAllMessages(): List<Pair<ChatUserInfo, ChatMessage>>? {
        return Collections.unmodifiableList(messages)
    }
}
