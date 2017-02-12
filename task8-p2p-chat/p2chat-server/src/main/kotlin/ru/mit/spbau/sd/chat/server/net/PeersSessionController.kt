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
import java.util.concurrent.ConcurrentHashMap


/**
 * Controls all peer sessions and dispatches messages, got from them
 */
internal class PeersSessionController(val peerEventHandler: PeerEventHandler<ChatUserIpAddr>):
        MessageListener<PeerToServerMsg, AsyncServer<PeerToServerMsg, ServerToPeerMsg, ChatUserIpAddr>>
{
    companion object {
        val logger = LoggerFactory.getLogger(PeersSessionController::class.java.simpleName)!!
    }

    private val idPeersMap = 
            ConcurrentHashMap<ChatUserIpAddr, AsyncServer<PeerToServerMsg, ServerToPeerMsg, ChatUserIpAddr>>()


    fun addPreparedConnection(channel: AsynchronousSocketChannel, userId: ChatUserIpAddr) {
        val server = createPeerServer(channel, userId)
        idPeersMap[userId] = server
        server.startReading()
    }


    override fun messageReceived(msg: PeerToServerMsg,
                                 attachment: AsyncServer<PeerToServerMsg, ServerToPeerMsg, ChatUserIpAddr>) {
        val peerId = attachment.getHeldPayload()!!

        when (msg.msgType!!) {
            PeerToServerMsg.Type.CONNECT -> {
                logger.error("Unexpected connect message...")
            }
            PeerToServerMsg.Type.DISCONNECT -> {
                idPeersMap.remove(peerId)
                attachment.destroy()
            }
            PeerToServerMsg.Type.PEER_ONLINE -> {
                peerEventHandler.peerBecomeOnline(peerId, msg.userInfo!!)
            }
            PeerToServerMsg.Type.GET_AVAILABLE_USERS -> {
                val availableUsers = listToUsersList(peerEventHandler.usersRequested())
                val usersMessage = ServerToPeerMsg.newBuilder()
                        .setMsgType(ServerToPeerMsg.Type.AVAILABLE_USERS)
                        .setUsers(availableUsers)
                        .build()!!
                attachment.writeMessage(usersMessage)
            }
            PeerToServerMsg.Type.MY_INFO_CHANGED -> {
                peerEventHandler.peerChangedInfo(peerId, msg.userInfo!!)
            }
            PeerToServerMsg.Type.PEER_GONE_OFFLINE -> {
                peerEventHandler.peerGoneOffline(peerId)
            }
            PeerToServerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->server] message type!")
            }
        }
    }

    fun destroy() {
        idPeersMap.values.forEach { it.destroy() }
        idPeersMap.clear()
    }

    private fun createPeerServer(channel: AsynchronousSocketChannel, userId: ChatUserIpAddr):
            AsyncServer<PeerToServerMsg, ServerToPeerMsg, ChatUserIpAddr> {
        val newPeer = AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToServerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = this,
                payload = userId,
                serverName = "P2SConnSrv[${userId.port}]"
        )
        return newPeer
    }
}
