package ru.mit.spbau.sd.chat.client.model

import ru.mit.spbau.sd.chat.client.ChatUserAlreadyExists
import ru.mit.spbau.sd.chat.client.ChatUserDoesNotExists
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Chat data holder.
 * Chat data contains:
 *     1. Map of online users
 *     2. Message box for each online user
 */
class ChatModel<T>(
        val clientId: T,
        var clientInfo: ChatUserInfo
) {
    private val usersMsgBoxes: MutableMap<T, MutableList<Pair<T, ChatMessage>>> = ConcurrentHashMap()
    private val usersMap: MutableMap<T, ChatUserInfo> = ConcurrentHashMap()



    private fun createEmptyMsgBox(): MutableList<Pair<T, ChatMessage>> {
        return ArrayList()
    }

    init {
        for (userId in usersMap.keys) {
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

    fun getUserInfo(userId: T): ChatUserInfo? {
        return usersMap[userId]
    }

    /**
     * Remove user from client chat
     */
    fun removeUser(userId: T) {
        if (userId !in usersMap) {
            throw ChatUserDoesNotExists("$userId")
        }
        usersMap.remove(userId)
        usersMsgBoxes.remove(userId)
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
    fun addMessageSentByThisUser(recipient: T, message: ChatMessage) {
        if (recipient !in usersMap) {
            throw ChatUserDoesNotExists("$recipient")
        }
        usersMsgBoxes[recipient]!!.add(Pair(clientId, message))
    }

    /**
     * "Receive" message from sender
     */
    fun addMessageSentByOtherUser(sender: T, message: ChatMessage) {
        if (sender !in usersMap) {
            throw ChatUserDoesNotExists("$sender")
        }
        usersMsgBoxes[sender]!!.add(Pair(sender, message))
    }

    /**
     * Get message history with given recipient/sender
     */
    fun getMessages(recepient: T): List<Pair<T, ChatMessage>> {
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

    /**
     * Deletes all users and all messages
     */
    fun clear() {
        usersMsgBoxes.clear()
        usersMap.clear()
    }
}
