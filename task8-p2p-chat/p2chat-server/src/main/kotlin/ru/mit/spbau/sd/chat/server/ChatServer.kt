package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.server.net.PeerConnectionManager
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel


class ChatServer(port: Int, msgListener: UserMapPeerMsgListener) {
    private val peerSessionController = PeersSessionController(msgListener)
    private val peerConnectionManager = PeerConnectionManager(peerSessionController)
    private val connectionAcceptor = AsyncConnectionAcceptor(
            AsynchronousServerSocketChannel.open().bind(InetSocketAddress(port)),
            peerConnectionManager
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