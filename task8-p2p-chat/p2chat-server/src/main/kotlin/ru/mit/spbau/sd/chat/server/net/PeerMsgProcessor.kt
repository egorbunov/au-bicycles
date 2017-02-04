package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersList

/**
 *
 */
internal interface PeerMsgProcessor <in T> {
    fun peerBecomeOnline(peer: T, userInfo: ChatUserInfo)
    fun peerGoneOffline(peer: T)
    fun peerChangedInfo(peer: T, newInfo: ChatUserInfo)
    fun usersRequested(): UsersList
    fun peerDisconnected(peer: T)
}
