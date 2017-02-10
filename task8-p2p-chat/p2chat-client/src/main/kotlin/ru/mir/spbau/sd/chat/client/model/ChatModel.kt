package ru.mir.spbau.sd.chat.client.model

import ru.mir.spbau.sd.chat.client.ChatUserAlreadyExists
import ru.mir.spbau.sd.chat.client.ChatUserDoesNotExists
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.util.*

/**
 * Chat data holder.
 * Chat data contains:
 *     1. Map of online users
 *     2. Message box for each online user
 */
class ChatModel<T>(
        private val clientId: T,
        private var clientInfo: ChatUserInfo,
        private val usersMap: AbstractMap<T, ChatUserInfo>
) {
    private val usersMsgBoxes = HashMap<T, MutableList<ChatMessage>>()

    private fun createEmptyMsgBox(): MutableList<ChatMessage> {
        return ArrayList()
    }

    init {
        for ((userId) in usersMap) {
            usersMsgBoxes[userId] = createEmptyMsgBox()
        }
    }

    /**
     * Add new user to client chat
     */
    fun addUser(userId: T, userInfo: ChatUserInfo) {
        if (userId in usersMap) {
            throw ChatUserAlreadyExists("$userId: ${usersMap[userId]}")
        }

        usersMap[userId] = userInfo
        usersMsgBoxes[userId] = createEmptyMsgBox()
    }

    /**
     * Remove user from client chat
     */
    fun removeUser(userId: T) {
        if (userId !in usersMap) {
            throw ChatUserDoesNotExists("$userId")
        }

        usersMap.remove(userId)
    }

    /**
     * Edit client chat user info
     */
    fun editUser(userId: T, newUserInfo: ChatUserInfo) {
        if (userId !in usersMap) {
            throw ChatUserDoesNotExists("$userId")
        }

        usersMap[userId] = newUserInfo
    }

    /**
     * Get all chat users (except current one)
     */
    fun getUsers(): List<Pair<T, ChatUserInfo>> {
        return usersMap.toList()
    }

    /**
     * "Sent" message to recipient
     */
    fun addMessageTo(recipient: T, message: ChatMessage) {
        if (recipient !in usersMap) {
            throw ChatUserDoesNotExists("$recipient")
        }
        usersMsgBoxes[recipient]!!.add(message)
    }

    /**
     * "Receive" message from sender
     */
    fun addMessageFrom(sender: T, message: ChatMessage) {
        if (sender !in usersMap) {
            throw ChatUserDoesNotExists("$sender")
        }
        usersMsgBoxes[sender]!!.add(message)
    }

    /**
     * Get message history with given recipient/sender
     */
    fun getMessages(recepient: T): List<ChatMessage> {
        if (recepient !in usersMap) {
            throw ChatUserDoesNotExists("$recepient")
        }
        return usersMsgBoxes[recepient]!!
    }

    /**
     * Change current client information
     */
    fun changeClientInfo(newInfo: ChatUserInfo) {
        clientInfo = newInfo
    }
}
