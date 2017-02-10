package ru.mir.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mir.spbau.sd.chat.client.msg.UsersEventHandler
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.net.*
import ru.mit.spbau.sd.chat.commons.p2pDisconnectMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.util.*

/**
 * Holder and controller of all client connections to other clients
 */
internal class UsersSessionsController(val usersEventHandler: UsersEventHandler<ChatUserIpAddr>) :
        MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>,
        AsyncConnectionListener {
    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java)!!
    }
    private val peers = ArrayList<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()
    private val peersIdMap = HashMap<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>, ChatUserIpAddr>()
    private val idPeersMap = HashMap<ChatUserIpAddr, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()

    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("New connection established with peer!")
        // creating session - async. single connection server for exact one peer
        val newPeer = AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToPeerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = this
        )
        peers.add(newPeer)
    }


    override fun messageReceived(msg: PeerToPeerMsg,
                                 attachment: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        when (msg.msgType!!) {
            PeerToPeerMsg.Type.CONNECT -> {
                if (attachment in peersIdMap) {
                    throw ProtocolViolation("Client is already connected [protocol violation]")
                }
                peersIdMap[attachment] = msg.userFromId!!
                idPeersMap[msg.userFromId!!] = attachment
            }
            PeerToPeerMsg.Type.DISCONNECT -> {
                checkClientConnected(attachment)
                destroyConnection(attachment)
            }
            PeerToPeerMsg.Type.I_AM_ONLINE -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandler.userBecomeOnline(peerId, msg.userInfo!!)
            }
            PeerToPeerMsg.Type.MY_INFO_CHANGED -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandler.userChangeInfo(peerId, msg.userInfo!!)
            }
            PeerToPeerMsg.Type.I_AM_GONE_OFFLINE -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandler.userGoneOffline(peerId)
            }
            PeerToPeerMsg.Type.TEXT_MESSAGE -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandler.userSentMessage(peerId, msg.message!!)
            }
            PeerToPeerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->peer] message type!")
            }
        }
    }

    private fun destroyConnection(conn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        peers.remove(conn)
        idPeersMap.remove(peersIdMap[conn])
        peersIdMap.remove(conn)
        conn.writeMessage(p2pDisconnectMsg(),
                onComplete = { conn.destroy() },
                onFail = { e->
                    logger.error("Failed to destroy user connection: $e")
                })
        conn.destroy()
    }

    /**
     * destroys all connections
     */
    fun destroy() {
        peers.forEach { destroyConnection(it) }
        peersIdMap.clear()
        idPeersMap.clear()
    }

    fun getOneUserConnection(userId: ChatUserIpAddr): AsyncServer<PeerToPeerMsg, PeerToPeerMsg>? {
        return idPeersMap[userId]
    }

    /**
     * Checks if client sent CONNECT message already; This check is performed
     * before any other user interaction with server
     */
    private fun checkClientConnected(client: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>): ChatUserIpAddr {
        if (client !in peersIdMap) {
            throw ProtocolViolation("Bad client (session not properly started)")
        }
        return peersIdMap[client]!!
    }
}
