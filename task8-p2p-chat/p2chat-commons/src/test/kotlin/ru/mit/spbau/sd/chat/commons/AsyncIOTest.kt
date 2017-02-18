package ru.mit.spbau.sd.chat.commons

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.asyncRead
import ru.mit.spbau.sd.chat.commons.net.asyncWrite
import ru.mit.spbau.sd.chat.commons.net.createStartReadingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel

class AsyncIOTest {
    val clientSock: AsynchronousSocketChannel = AsynchronousSocketChannel.open()
    val serverSock: AsynchronousSocketChannel

    init {
        val acceptingSock = AsynchronousServerSocketChannel.open()
        acceptingSock.bind(InetSocketAddress(InetAddress.getLocalHost(), 0))
        val connFuture = acceptingSock.accept()!!
        clientSock.connect(acceptingSock.localAddress)
        serverSock = connFuture.get()!!
    }

    @Test
    fun testReadFuture() {
        val msg = "hello".repeat(10000)
        asyncWrite(clientSock, createStartWritingState(msg.toByteArray()), {}, {})
        val readFuture = asyncRead(serverSock, createStartReadingState { String(it) })
        Assert.assertEquals(msg, readFuture.get())
    }
}
