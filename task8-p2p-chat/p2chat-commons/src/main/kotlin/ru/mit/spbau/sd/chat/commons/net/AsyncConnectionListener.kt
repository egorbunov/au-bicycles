package ru.mit.spbau.sd.chat.commons.net

import java.nio.channels.AsynchronousSocketChannel


interface AsyncConnectionListener {
    fun connectionEstablished(channel: AsynchronousSocketChannel)
}