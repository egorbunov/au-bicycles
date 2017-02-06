package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersList

/**
 * Users map
 */
interface UsersMap<in T> {
    /**
     * Returns available users
     */
    fun getUsers(): UsersList
    fun addUser(userId: T, userInfo: ChatUserInfo)
    fun removeUser(userId: T)
    fun editUser(userId: T, newUserInfo: ChatUserInfo)
}
