package ru.mit.spbau.sd.chat.server

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersInfo

/**
 * Interface to server-side chat data
 */
interface ChatModelInterface {
    /**
     * Returns available users
     */
    fun getUsers(): UsersInfo
    fun addUser(userId: String, userInfo: ChatUserInfo)
    fun removeUser(userId: String)
    fun editUser(userId: String, newUserInfo: ChatUserInfo)
}
