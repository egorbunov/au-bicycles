package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.listToUsersList
import ru.mit.spbau.sd.chat.commons.net.*
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.util.*


/**
 * Controls all peer sessions and dispatches messages, got from them
 */
internal class PeersSessionController(val peerEventHandler: PeerEventHandler<ChatUserIpAddr>):
        MessageListener<PeerToServerMsg, AsyncServer<PeerToServerMsg, ServerToPeerMsg>>,
        AsyncConnectionListener
{

    companion object {
        val logger = LoggerFactory.getLogger(PeersSessionController::class.java.simpleName)!!
    }
    private val peers = ArrayList<AsyncServer<PeerToServerMsg, ServerToPeerMsg>>()
    private val peersIdMap = HashMap<AsyncServer<PeerToServerMsg, ServerToPeerMsg>, ChatUserIpAddr>()
    private val idPeersMap = HashMap<ChatUserIpAddr, AsyncServer<PeerToServerMsg, ServerToPeerMsg>>()

    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("New connection established with peer!")
        // creating session - async. single connection server for exact one peer
        val newPeer = AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToServerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = this,
                serverName = "Peer2ServerConnServer"
        )
        newPeer.startReading()
        peers.add(newPeer)
    }


    override fun messageReceived(msg: PeerToServerMsg,
                                 attachment: AsyncServer<PeerToServerMsg, ServerToPeerMsg>) {
        when (msg.msgType!!) {
            PeerToServerMsg.Type.CONNECT -> {
                if (attachment in peersIdMap) {
                    throw ProtocolViolation("Client is already connected [protocol violation]")
                }
                peersIdMap[attachment] = msg.userId!!
                idPeersMap[msg.userId!!] = attachment
                attachment.writeMessageSync(
                        ServerToPeerMsg.newBuilder()
                                .setMsgType(ServerToPeerMsg.Type.CONNECT_OK)
                                .build()
                )
            }
            PeerToServerMsg.Type.DISCONNECT -> {
                checkClientConnected(attachment)
                peers.remove(attachment)
                idPeersMap.remove(peersIdMap[attachment])
                peersIdMap.remove(attachment)
                attachment.destroy()
            }
            PeerToServerMsg.Type.PEER_ONLINE -> {
                val peerId = checkClientConnected(attachment)
                peerEventHandler.peerBecomeOnline(peerId, msg.userInfo!!)
            }
            PeerToServerMsg.Type.GET_AVAILABLE_USERS -> {
                checkClientConnected(attachment)
                val availableUsers = listToUsersList(peerEventHandler.usersRequested())
                val usersMessage = ServerToPeerMsg.newBuilder()
                        .setMsgType(ServerToPeerMsg.Type.AVAILABLE_USERS)
                        .setUsers(availableUsers)
                        .build()!!
                attachment.writeMessage(usersMessage)
            }
            PeerToServerMsg.Type.MY_INFO_CHANGED -> {
                val peerId = checkClientConnected(attachment)
                peerEventHandler.peerChangedInfo(peerId, msg.userInfo!!)
            }
            PeerToServerMsg.Type.PEER_GONE_OFFLINE -> {
                val peerId = checkClientConnected(attachment)
                peerEventHandler.peerGoneOffline(peerId)
            }
            PeerToServerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->server] message type!")
            }
        }
    }

    /**
     * Checks if client sent CONNECT message already; This check is performed
     * before any other user interaction with server
     */
    private fun checkClientConnected(client: AsyncServer<PeerToServerMsg, ServerToPeerMsg>): ChatUserIpAddr {
        if (client !in peersIdMap) {
            throw ProtocolViolation("Bad client (session not properly started)")
        }
        return peersIdMap[client]!!
    }

    fun destroy() {
        peersIdMap.clear()
        idPeersMap.clear()
        for (peer in peers) {
            peer.destroy()
        }
    }

    fun getOneUserConnection(userId: ChatUserIpAddr): AsyncServer<PeerToServerMsg, ServerToPeerMsg>? {
        return idPeersMap[userId]
    }
}
