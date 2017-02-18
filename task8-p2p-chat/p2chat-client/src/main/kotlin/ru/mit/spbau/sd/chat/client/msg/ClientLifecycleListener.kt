package ru.mit.spbau.sd.chat.client.msg

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr

/**
 * This listener is used for subscription to major client lifecycle events
 * This interface is needed as long as this events may occur randomly due
 * to asynchronous nature
 */
interface ClientLifecycleListener {
    /**
     * Event, which occurs at the very beginning of the client live, when it
     * connects to peer-server and gets the list of available users from it
     */
    fun clientStarted(usersList: List<Pair<ChatUserIpAddr, ChatUserInfo>>)

    /**
     * Event, which is generated after client does all it's job to correctly
     * leave chat-network
     */
    fun clientStopped()
}