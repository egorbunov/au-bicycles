package ru.mit.spbau.sd.chat.server.net

import ru.mit.spbau.sd.chat.commons.inetSockAddrToUserIp
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*

/**
 * Simple peer message processor, which delegates all events to
 * chat model
 */
class UserMapPeerMsgListener(private val usersMap: AbstractMap<InetSocketAddress, ChatUserInfo>)
    : PeerEventHandler<ChatUserIpAddr> {

    override fun peerBecomeOnline(userId: ChatUserIpAddr, userInfo: ChatUserInfo) {
        val id = chatUserIpAddrToSockAddr(userId)
        if (id in usersMap) {
            throw IllegalStateException("User already online")
        }
        usersMap[id] = userInfo
    }

    override fun peerGoneOffline(userId: ChatUserIpAddr) {
        val id = chatUserIpAddrToSockAddr(userId)
        if (id !in usersMap)
        usersMap.remove(id)
    }

    override fun peerChangedInfo(userId: ChatUserIpAddr, newInfo: ChatUserInfo) {
        val id = chatUserIpAddrToSockAddr(userId)
        usersMap[id] = newInfo
    }

    override fun usersRequested(): List<Pair<ChatUserIpAddr, ChatUserInfo>> {
        val users = usersMap.toList()
        return users.map { p ->
            Pair(
                    inetSockAddrToUserIp(p.first),
                    p.second
            )
        }
    }
}
