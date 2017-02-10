package ru.mir.spbau.sd.chat.client.msg

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo

/**
 * Listener for all protocol-specific chat events, which may arrive
 * from network.
 */
interface UsersEventHandler<in T> {
    fun userBecomeOnline(userId: T, userInfo: ChatUserInfo)
    fun userGoneOffline(userId: T)
    fun userChangeInfo(userId: T, newInfo: ChatUserInfo)
    fun userSentMessage(userId: T, message: ChatMessage)
}
