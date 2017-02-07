package ru.mit.spbau.sd.chat.server.net

import ru.mit.spbau.sd.chat.commons.UsersMap
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersList
import java.net.InetSocketAddress

/**
 * Simple peer message processor, which delegates all events to
 * chat model
 */
class ChatModelPeerMsgProcessor(private val chatModel: UsersMap<InetSocketAddress>)
    : PeerMsgListener<InetSocketAddress> {
    override fun peerBecomeOnline(userId: InetSocketAddress, userInfo: ChatUserInfo) {
        chatModel.addUser(userId, userInfo)
    }

    override fun peerGoneOffline(userId: InetSocketAddress) {
        chatModel.removeUser(userId)
    }

    override fun peerChangedInfo(userId: InetSocketAddress, newInfo: ChatUserInfo) {
        chatModel.editUser(userId, newInfo)
    }

    override fun usersRequested(): UsersList {
        return chatModel.getUsers()
    }
}
