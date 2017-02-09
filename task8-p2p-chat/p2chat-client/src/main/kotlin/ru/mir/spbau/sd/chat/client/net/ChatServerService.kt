package ru.mir.spbau.sd.chat.client.net

import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel

/**
 * Class, which provides interface to chat server
 */
class ChatServerService(val serverAddress: SocketAddress) {
    fun x() {
        val s = AsynchronousSocketChannel.open()
    }
}