package ru.mit.spbau.sd.chat.server.net

import ru.mit.spbau.sd.chat.server.ChatModelInterface
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.UsersInfo

/**
 * Simple peer message processor, which delegates all events to
 * chat model
 */
class ChatModelPeerMsgProcessor(private val chatModel: ChatModelInterface) : PeerMsgProcessor<String> {
    override fun peerBecomeOnline(peer: String, userInfo: ChatUserInfo) {
        chatModel.addUser(peer, userInfo)
    }

    override fun peerGoneOffline(peer: String) {
        chatModel.removeUser(peer)
    }

    override fun peerChangedInfo(peer: String, newInfo: ChatUserInfo) {
        chatModel.editUser(peer, newInfo)
    }

    override fun usersRequested(): UsersInfo {
        return chatModel.getUsers()
    }

    override fun peerDisconnected(peer: String) {
    }
}