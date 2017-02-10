package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import java.net.SocketAddress


class ChatServer(port: Int, msgListener: UserMapPeerMsgListener) {
    private val peerSessionController = PeersSessionController(msgListener)
    private val connectionAcceptor = AsyncConnectionAcceptor(
            port,
            peerSessionController
    )

    fun address(): SocketAddress {
        return connectionAcceptor.getAddress()
    }

    fun start() {
        connectionAcceptor.start()
    }

    fun stop() {
        connectionAcceptor.destroy()
        peerSessionController.destroy()
    }
}