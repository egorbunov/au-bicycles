package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersList

/**
 *
 */
internal interface PeerMsgListener<in T> {
    fun peerBecomeOnline(userId: T, userInfo: ChatUserInfo)
    fun peerGoneOffline(userId: T)
    fun peerChangedInfo(userId: T, newInfo: ChatUserInfo)
    fun usersRequested(): UsersList
}
