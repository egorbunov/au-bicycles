package ru.mit.spbau.sd.chat.client.msg

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo

/**
 * Listener for all protocol-specific chat events, which may arrive
 * from network.
 */
interface UsersNetEventHandler<in T> {
    /**
     * Event means that user have successfully connected to
     * peer server and is ready for chatting
     */
    fun userBecomeOnline(userId: T, userInfo: ChatUserInfo)

    /**
     * User is no longer available for chatting
     */
    fun userGoneOffline(userId: T)

    /**
     * User have changed it's personal details
     */
    fun userChangeInfo(userId: T, newInfo: ChatUserInfo)

    /**
     * User have something for you to read =)
     */
    fun userSentMessage(userId: T, message: ChatMessage)
}
