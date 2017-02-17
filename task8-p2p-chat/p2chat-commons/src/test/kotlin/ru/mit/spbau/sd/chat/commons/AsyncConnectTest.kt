package ru.mit.spbau.sd.chat.commons

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Ignore
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionListener
import ru.mit.spbau.sd.chat.commons.net.asyncConnect
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

/**
 * Created by: Egor Gorbunov
 * Date: 2/9/17
 * Email: egor-mailbox@ya.com
 */

class AsyncConnectTest {
    @Test
    @Ignore
    fun testCallbackInvoked() {
        val serverConnListener: AsyncConnectionListener = mock()
        val clientConnListener: AsyncConnectionListener = mock()

        // server
        val connAcceptor = AsyncConnectionAcceptor(
                AsynchronousServerSocketChannel.open().bind(InetSocketAddress(0)),
                serverConnListener
        )
        connAcceptor.start()

        // client
        asyncConnect(connAcceptor.getAddress(), clientConnListener)

        /*
            I'am not sure about correctness of this test; But I hope, that
            1 second is enough for async. connect to propagate to complete
            handler in some way
         */
        Thread.sleep(1000)

        verify(serverConnListener).connectionEstablished(any())
        verify(clientConnListener).connectionEstablished(any())

        connAcceptor.destroy()
    }

    @Ignore
    @Test
    fun testConnectWithFuture() {
        val serverConnListener: AsyncConnectionListener = mock()

        // server
        val connAcceptor = AsyncConnectionAcceptor(
                AsynchronousServerSocketChannel.open().bind(InetSocketAddress(0)),
                serverConnListener
        )
        connAcceptor.start()

        // client
        asyncConnect(connAcceptor.getAddress()).get()
        Thread.sleep(500) // ...
        verify(serverConnListener).connectionEstablished(any())

        connAcceptor.destroy()
    }
}
