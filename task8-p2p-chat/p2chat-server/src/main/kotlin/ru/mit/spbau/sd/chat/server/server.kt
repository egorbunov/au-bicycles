package ru.mit.spbau.sd.chat.server

import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("USAGE: java -jar server.jar [PORT NUMBER]")
        return
    }

    val chatModel = ConcurrentHashMap<InetSocketAddress, ChatUserInfo>()
    val modelChanger = UserMapPeerMsgListener(chatModel)

    val server = try {
        ChatServer(args[0].toInt(), modelChanger)
    } catch (e: Exception) {
        println("Port is an integer number!")
        return
    }


    server.start()
    val addr = server.address() as InetSocketAddress
    println("Server running at: ${addr.hostName}:${addr.port}")
    readLine()
    server.stop()
}
