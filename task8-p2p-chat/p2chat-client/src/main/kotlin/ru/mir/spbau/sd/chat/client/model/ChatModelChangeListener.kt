package ru.mir.spbau.sd.chat.client.model

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo

/**
 * Model listener interface, which is triggered by `ChatModel`
 */
interface ChatModelChangeListener <T> {
    /**
     * Triggered in case new user added to chat model; This event
     * has meaning: user become online
     */
    fun newUserAdded(userId: T, userInfo: ChatUserInfo)

    /**
     * Triggered in case user gone offline (deleted from model)
     */
    fun userRemoved(userId: T)

    /**
     * Triggered if user information has changed
     */
    fun userChanged(userId: T, newInfo: ChatUserInfo)

    /**
     * Triggered in case new message was added to message box from sender
     * side (it was received!)
     */
    fun messageReceived(senderId: T, message: ChatMessage)

    /**
     * Triggered in case new message was added by current client, so it
     * was sent
     */
    fun messageSent(recipientId: T, message: ChatMessage)

    /**
     * This client info change trigger
     */
    fun currentClientInfoChanged(newInfo: ChatUserInfo)
}
