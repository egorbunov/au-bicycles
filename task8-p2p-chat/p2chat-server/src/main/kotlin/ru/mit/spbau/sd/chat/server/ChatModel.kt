package ru.mit.spbau.sd.chat.server

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersList

/**
 * Interface to server-side chat data
 */
interface ChatModel<in T> {
    /**
     * Returns available users
     */
    fun getUsers(): UsersList
    fun addUser(userId: T, userInfo: ChatUserInfo)
    fun removeUser(userId: T)
    fun editUser(userId: T, newUserInfo: ChatUserInfo)
}
