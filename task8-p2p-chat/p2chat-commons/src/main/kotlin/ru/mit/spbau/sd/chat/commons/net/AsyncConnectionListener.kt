package ru.mit.spbau.sd.chat.commons.net

import java.nio.channels.AsynchronousSocketChannel

/**
 * Simple listener, which will be notified in case new connection
 * was established.
 */
interface AsyncConnectionListener {
    fun connectionEstablished(channel: AsynchronousSocketChannel)
}