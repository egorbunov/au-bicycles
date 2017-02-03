package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersInfo
import java.net.SocketAddress

/**
 *
 */
internal interface PeerEventProcessor {
    /**
     * event, which occurs when user sets up its info for the first time
     * (it likely happens just after connection accept)
     */
    fun startChatting(peerAddress: SocketAddress, userInfo: ChatUserInfo)

    fun disconnectPeer(peer: OnePeerServer)
    fun peerChangedInfo(peerAddress: SocketAddress, newInfo: ChatUserInfo)
    fun usersRequested(peer: OnePeerServer)
}
