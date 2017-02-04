package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.server.ChatModelInterface
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import ru.spbau.mit.sd.commons.proto.UsersInfo
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.*

/**
 * Asynchronous server, which accepts connections from chat users
 *
 * @param modelPeerMsgProcessor - peer message processor, which does some
 *        useful job with messages from clients
 */
class ChatServer(private val modelPeerMsgProcessor: ChatModelPeerMsgProcessor) {
    companion object {
        val logger = LoggerFactory.getLogger(ChatServer::class.java.canonicalName)!!
    }

    /**
     * Method, which makes server to listen at some port for peer connections
     * This method is non-blocking
     * It must be called at the very beginning of the work with server
     */
    fun setup()  {
        serverSocket = AsynchronousServerSocketChannel.open()
        serverSocket!!.bind(null)
        setupAcceptor()
    }

    /**
     * One might call this method after all server work is done to
     * close all connection's sockets and listening socket
     */
    fun destroy() {
        if (serverSocket != null) {
            serverSocket!!.close()
        }
        for (peer in peers) {
            peer.destroy()
        }
        peers.clear()
    }


    private fun setupAcceptor() {
        serverSocket!!.accept(null, object: CompletionHandler<AsynchronousSocketChannel, Nothing?> {
            override fun failed(exc: Throwable?, attachment: Nothing?) {
                logger.error("Accept failed: $exc")
            }

            override fun completed(result: AsynchronousSocketChannel?, attachment: Nothing?) {
                logger.info("Accepted connection from peer: ${result!!.remoteAddress}")
                val newPeerServer = OnePeerServer(result, peerEventProcessor)
                newPeerServer.start()

                peers.add(newPeerServer)

                // subscribe on accept again
                serverSocket!!.accept(null, this)
            }

        })
    }

    private var serverSocket: AsynchronousServerSocketChannel? = null
    private val peers = ArrayList<OnePeerServer>()

    /**
     * Each peer connection sits and waits for messages from peers and
     * on this messages whole server should react somehow, so this is
     * done with help of this anonymous event processor instance
     */
    private val peerEventProcessor = object: PeerMsgProcessor<OnePeerServer> {
        private fun peerServerToStringId(peer: OnePeerServer): String {
            return socketAddrToId(peer.channel.remoteAddress as InetSocketAddress)
        }

        override fun peerBecomeOnline(peer: OnePeerServer, userInfo: ChatUserInfo) {
            modelPeerMsgProcessor.peerBecomeOnline(peerServerToStringId(peer), userInfo)
        }

        override fun peerChangedInfo(peer: OnePeerServer, newInfo: ChatUserInfo) {
            modelPeerMsgProcessor.peerChangedInfo(peerServerToStringId(peer), newInfo)
        }

        override fun peerDisconnected(peer: OnePeerServer) {
            modelPeerMsgProcessor.peerDisconnected(peerServerToStringId(peer))
            // cancelling connection with peer
            peer.destroy()
            peers.remove(peer)
        }

        /**
         * Sends message to each peer designating, that one of chat members
         * have disconnected
         */
        override fun peerGoneOffline(peer: OnePeerServer) {
            modelPeerMsgProcessor.peerGoneOffline(peerServerToStringId(peer))
        }

        /**
         * Method, which creates response payload for peer request
         * to get all current chat users
         */
        override fun usersRequested(): UsersInfo {
            return modelPeerMsgProcessor.usersRequested()
        }

    }
}
