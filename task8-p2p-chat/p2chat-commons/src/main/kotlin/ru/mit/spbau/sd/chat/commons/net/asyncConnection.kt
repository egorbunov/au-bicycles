package ru.mit.spbau.sd.chat.commons.net

import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

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

    /**
     * Returns server local address
     */
    fun getAddress(): SocketAddress = serverSocket.localAddress!!
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
            logger.debug("Connection wit $address established successfully")
            connectionListener.connectionEstablished(connection)
        }

        override fun failed(exc: Throwable?, attachment: Nothing?) {
            logger.error("Async. connect failed, dest: $address")
        }
    })
}

/**
 * Same as above, but returning future
 */
fun asyncConnect(address: SocketAddress): Future<AsynchronousSocketChannel> {
    val connection = AsynchronousSocketChannel.open()
    val future = connection.connect(address)
    return object: Future<AsynchronousSocketChannel> {
        override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
            return future.cancel(mayInterruptIfRunning)
        }
        override fun get(): AsynchronousSocketChannel {
            future.get()
            return connection
        }
        override fun get(timeout: Long, unit: TimeUnit): AsynchronousSocketChannel {
            future.get(timeout, unit)
            return connection
        }
        override fun isCancelled() = future.isCancelled
        override fun isDone() = future.isDone

    }
}
