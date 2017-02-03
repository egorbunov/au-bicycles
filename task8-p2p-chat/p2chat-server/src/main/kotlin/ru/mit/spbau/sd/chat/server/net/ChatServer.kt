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
 * @param chatModel - data, which is held by chat server, this data will
 *        be changed during busy server life
 */
class ChatServer(val chatModel: ChatModelInterface) {
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
    private val peerEventProcessor = object: PeerEventProcessor {
        /**
         * Sends message with new info for changed peer.
         * Message is sent to each peer, except one, which made the change.
         */
        override fun peerChangedInfo(peerAddress: SocketAddress, newInfo: ChatUserInfo) {
            val remotePeerAddress = peerAddress as InetSocketAddress
            val peerId = socketAddrToId(remotePeerAddress)
            chatModel.editUser(peerId, newInfo)

            // constructing client info change message
            val message = ServerToPeerMsg.newBuilder()
                    .setMsgType(ServerToPeerMsg.Type.PEER_CHANGED_NAME)
                    .setPeerAddr(
                            ChatUserIpAddr.newBuilder()
                                    .setIp(remotePeerAddress.address.hostAddress)
                                    .setPort(remotePeerAddress.port)
                    )
                    .setPeerInfo(newInfo)
                    .build()

            peers
                    .filter { it.channel.remoteAddress != peerAddress }
                    .forEach { it.sendMessage(message) }

        }

        /**
         * Sends message to each peer designating, that one of chat members
         * have disconnected
         */
        override fun disconnectPeer(peer: OnePeerServer) {
            val remotePeerAddress = peer.channel.remoteAddress as InetSocketAddress
            val peerId = socketAddrToId(remotePeerAddress)
            chatModel.removeUser(peerId)
            if (!peers.remove(peer)) {
                throw IllegalStateException("Can't remove given peer, it does not exist")
            }

            // constructing disconnect message
            val message = ServerToPeerMsg.newBuilder()
                    .setMsgType(ServerToPeerMsg.Type.PEER_DISCONNECTED)
                    .setPeerAddr(
                            ChatUserIpAddr.newBuilder()
                                    .setIp(remotePeerAddress.address.hostAddress)
                                    .setPort(remotePeerAddress.port)
                    )
                    .build()

            peers.forEach { it.sendMessage(message) }
        }

        /**
         * Method, which creates response payload for peer request
         * to get all current chat users
         */
        override fun usersRequested(peer: OnePeerServer) {
            val users = chatModel.getUsers()
            val responseMessage = ServerToPeerMsg.newBuilder()
                    .setMsgType(ServerToPeerMsg.Type.USERS_INFO_DATA)
                    .setUsersInfo(users)
                    .build()
            peer.sendMessage(responseMessage)
        }

    }
}