package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

fun main(args: Array<String>) {
    val chatModel = ConcurrentHashMap<InetSocketAddress, ChatUserInfo>()
    val modelChanger = UserMapPeerMsgListener(chatModel)
    val server = ChatServer(args[0].toInt(), modelChanger)


    server.start()
    val addr = server.address() as InetSocketAddress
    println("Server running at: ${addr.hostName}:${addr.port}")
    readLine()
    server.stop()
}
