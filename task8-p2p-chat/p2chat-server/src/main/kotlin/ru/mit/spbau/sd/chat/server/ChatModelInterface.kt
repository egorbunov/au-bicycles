package ru.mit.spbau.sd.chat.server

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersInfo

/**
 * Created by: Egor Gorbunov
 * Date: 2/3/17
 * Email: egor-mailbox@ya.com
 */
interface ChatModelInterface {
    fun getUsers(): UsersInfo
    fun addUser(userId: String, userInfo: ChatUserInfo)
    fun removeUser(userId: String)
    fun editUser(userId: String, newUserInfo: ChatUserInfo)
}
