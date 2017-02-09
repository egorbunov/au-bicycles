package ru.mit.spbau.sd.chat.commons.net

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

// TODO: think about error listener

/**
 * Asynchronous connection acceptor.
 *
 * @param port - port, where server socket will be bind
 * @param connectionListener - callback, invoked in case new connection established
 */
class AsyncConnectionAcceptor(val port: Int,
                           val connectionListener: AsyncConnectionListener) {
    companion object {
        val logger = LoggerFactory.getLogger(AsyncConnectionAcceptor::class.java)!!
    }

    private val serverSocket = AsynchronousServerSocketChannel.open()

    /**
     * Binds server socket to given port and starts listening for connections, asynchronously
     */
    fun start() {
        serverSocket.bind(InetSocketAddress(port))
        serverSocket.accept(null, object: CompletionHandler<AsynchronousSocketChannel, Nothing?> {
            override fun failed(exc: Throwable?, attachment: Nothing?) {
                logger.error("Accept failed: $exc")
            }

            override fun completed(result: AsynchronousSocketChannel?, attachment: Nothing?) {
                logger.info("Accepted connection from: ${result!!.remoteAddress}")
                connectionListener.connectionEstablished(result)
                // subscribe on accept again
                serverSocket!!.accept(null, this)
            }
        })
    }

    /**
     * Closes server socket and consequently stops listening for connections
     */
    fun destroy() {
        serverSocket.close()
    }
}

/**
 * Method to asynchronously establish connection with remote server.
 *
 * @param address server address
 * @param connectionListener
 */
fun asyncConnect(address: SocketAddress, connectionListener: AsyncConnectionListener) {
    val logger = LoggerFactory.getLogger("AsyncConnectMethod")!!

    val connection = AsynchronousSocketChannel.open()
    connection.connect(address, null, object: CompletionHandler<Void, Nothing?> {
        override fun completed(result: Void?, attachment: Nothing?) {
            connectionListener.connectionEstablished(connection)
        }

        override fun failed(exc: Throwable?, attachment: Nothing?) {
            logger.error("Async. connect failed, dest: $address")
        }
    })
}
