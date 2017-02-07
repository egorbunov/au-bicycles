package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.*

/**
 * Simple peer message processor, which delegates all events to
 * chat model
 */
class ChatModelPeerMsgProcessor(private val usersMap: AbstractMap<InetSocketAddress, ChatUserInfo>)
    : PeerMsgListener<InetSocketAddress> {
    override fun peerBecomeOnline(userId: InetSocketAddress, userInfo: ChatUserInfo) {
        if (userId in usersMap) {
            throw IllegalStateException("User already online")
        }
        usersMap[userId] = userInfo
    }

    override fun peerGoneOffline(userId: InetSocketAddress) {
        if (userId !in usersMap)
        usersMap.remove(userId)
    }

    override fun peerChangedInfo(userId: InetSocketAddress, newInfo: ChatUserInfo) {
        usersMap[userId] = newInfo
    }

    override fun usersRequested(): List<Pair<InetSocketAddress, ChatUserInfo>> {
        return usersMap.toList()
    }
}
