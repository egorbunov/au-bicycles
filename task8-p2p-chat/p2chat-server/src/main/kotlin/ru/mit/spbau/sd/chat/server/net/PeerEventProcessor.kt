package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersInfo
import java.net.SocketAddress

/**
 *
 */
internal interface PeerEventProcessor {
    fun disconnectPeer(peer: OnePeerServer)
    fun peerChangedInfo(peerAddress: SocketAddress, newInfo: ChatUserInfo)
    fun usersRequested(peer: OnePeerServer)
}
