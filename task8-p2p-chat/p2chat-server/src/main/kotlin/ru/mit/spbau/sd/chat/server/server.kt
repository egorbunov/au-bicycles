package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

fun main(args: Array<String>) {
    val chatModel = ConcurrentHashMap<InetSocketAddress, ChatUserInfo>()
    val modelChanger = UserMapPeerMsgListener(chatModel)
    val peerSessionController = PeersSessionController(modelChanger)

    val connectionAcceptor = AsyncConnectionAcceptor(
            0,
            peerSessionController
    )



    connectionAcceptor.start()

    readLine()

    connectionAcceptor.destroy()
    peerSessionController.destroy()
}
