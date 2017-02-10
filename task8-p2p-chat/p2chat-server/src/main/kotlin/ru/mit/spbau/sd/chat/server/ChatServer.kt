package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap


class ChatServer(port: Int) {
    private val chatModel = ConcurrentHashMap<InetSocketAddress, ChatUserInfo>()
    private val modelChanger = UserMapPeerMsgListener(chatModel)
    private val peerSessionController = PeersSessionController(modelChanger)
    private val connectionAcceptor = AsyncConnectionAcceptor(
            port,
            peerSessionController
    )

    fun start() {
        connectionAcceptor.start()
    }

    fun stop() {
        connectionAcceptor.destroy()
        peerSessionController.destroy()
    }
}