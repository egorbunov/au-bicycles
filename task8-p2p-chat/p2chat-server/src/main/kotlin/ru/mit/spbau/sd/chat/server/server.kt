package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.server.net.ChatModelPeerMsgProcessor
import ru.mit.spbau.sd.chat.server.net.ChatServer
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

fun main(args: Array<String>) {
    val chatModel = ConcurrentHashMap<InetSocketAddress, ChatUserInfo>()
    val peerEventProcessor = ChatModelPeerMsgProcessor(chatModel)
    val chatServer = ChatServer(peerEventProcessor)

    chatServer.setup()

    readLine()

    chatServer.destroy()
}
