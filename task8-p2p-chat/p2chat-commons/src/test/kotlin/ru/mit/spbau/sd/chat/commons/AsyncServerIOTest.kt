package ru.mit.spbau.sd.chat.commons

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.*
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.util.*
import java.util.concurrent.CountDownLatch


/**
 * Tests that server receives message from client
 * (That is more integration test than unit test, but whatever)
 */
class AsyncServerIOTest {
    var countDownLatch: CountDownLatch? = null

    // server side connection server
    var serverSideConnSrv: AsyncServer<String, String, Nothing>? = null
    // client side connection server
    var clientSideConnSrv: AsyncServer<String, String, Nothing>? = null

    /**
     * array with messages, received by server from client
     */
    val serverReceivedRequests: MutableList<String> = Collections.synchronizedList(ArrayList<String>())!!
    /**
     * array with messages, received by client from server
     */
    val clientReceivedResponses: MutableList<String> = Collections.synchronizedList(ArrayList<String>())!!


    val serverMessageListener = object: MessageListener<String, AsyncServer<String, String, Nothing>> {
        override fun messageReceived(msg: String, attachment: AsyncServer<String, String, Nothing>) {
            serverReceivedRequests.add(msg)
            countDownLatch!!.countDown()
        }
    }

    val clientMessageListener = object: MessageListener<String, AsyncServer<String, String, Nothing>> {
        override fun messageReceived(msg: String, attachment: AsyncServer<String, String, Nothing>) {
            clientReceivedResponses.add(msg)
            countDownLatch!!.countDown()

        }
    }

    val serverConnListener = object: AsyncConnectionListener {
        override fun connectionEstablished(channel: AsynchronousSocketChannel) {
            serverSideConnSrv = AsyncServer(
                    channel,
                    { createStartReadingState { String(it) } },
                    { str -> createStartWritingState(str.toByteArray()) },
                    serverMessageListener
            )
            countDownLatch!!.countDown()
        }
    }

    /**
     * Establishes one connection btw server and client
     */
    @Before
    fun setup() {
        serverReceivedRequests.clear()
        clientReceivedResponses.clear()

        // server side acceptor
        val connAcceptor = AsyncConnectionAcceptor(
                AsynchronousServerSocketChannel.open().bind(InetSocketAddress(InetAddress.getLocalHost(), 0)),
                serverConnListener
        )
        connAcceptor.start()

        countDownLatch = CountDownLatch(1)
        // client side connection
        val clientConnection = asyncConnect(connAcceptor.getAddress()).get()
        // waiting for server to establish connection with this client
        countDownLatch!!.await()

        clientSideConnSrv = AsyncServer(
                clientConnection,
                { createStartReadingState { String(it) }},
                { str: String -> createStartWritingState(str.toByteArray())},
                clientMessageListener
        )
        serverSideConnSrv!!.startReading()
        clientSideConnSrv!!.startReading()

        connAcceptor.destroy()
    }

    @After
    fun after() {
        serverSideConnSrv!!.destroy()
        clientSideConnSrv!!.destroy()
    }

    @Test
    fun testClientServerIO() {
        /*
            Testing client->server message delivery
         */
        val clientServerMsg = "hello, server"
        countDownLatch = CountDownLatch(1)
        clientSideConnSrv!!.writeMessage(clientServerMsg)
        // waiting for server to receive message
        countDownLatch!!.await()

        Assert.assertEquals(1, serverReceivedRequests.size)
        Assert.assertEquals(clientServerMsg, serverReceivedRequests[0])

        /*
            Testing server->client message delivery
         */
        val serverClientMsg = "hello, client"
        countDownLatch = CountDownLatch(1)
        serverSideConnSrv!!.writeMessage(serverClientMsg)
        // waiting for client to receive response
        countDownLatch!!.await()

        Assert.assertEquals(1, clientReceivedResponses.size)
        Assert.assertEquals(serverClientMsg, clientReceivedResponses[0])
    }

    @Test
    fun testManyWrites() {
        val clientServerMsgs = (0..1000).map(Int::toString)
        countDownLatch = CountDownLatch(clientServerMsgs.size)
        clientServerMsgs.forEach {
            clientSideConnSrv!!.writeMessage(it)
        }
        countDownLatch!!.await()

        Assert.assertEquals(clientServerMsgs, serverReceivedRequests)
    }

    @Test
    fun testSyncWrite() {
        countDownLatch = CountDownLatch(1)
        val message = "hello"
        clientSideConnSrv!!.writeMessageSync(message)
        countDownLatch!!.await()

        Assert.assertEquals(1, serverReceivedRequests.size)
        Assert.assertEquals(message, serverReceivedRequests[0])
    }

    @Test
    fun testBigMessage() {
        countDownLatch = CountDownLatch(1)
        val bigMessage = "x".repeat(10000000)
        clientSideConnSrv!!.writeMessage(bigMessage)
        countDownLatch!!.await()

        Assert.assertEquals(1, serverReceivedRequests.size)
        Assert.assertEquals(bigMessage, serverReceivedRequests[0])
    }
}
