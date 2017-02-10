package ru.mir.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mir.spbau.sd.chat.client.msg.UsersEventHandler
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.net.*
import ru.mit.spbau.sd.chat.commons.p2pConnectMsg
import ru.mit.spbau.sd.chat.commons.p2pDisconnectMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.util.*

/**
 * Holder and controller of all client connections to other clients
 */
internal class UsersSessionsController(
        val currentClientId: ChatUserIpAddr,
        val usersEventHandler: UsersEventHandler<ChatUserIpAddr>
) :
        MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>,
        AsyncConnectionListener {
    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java)!!
    }
    private val users = ArrayList<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()
    private val userIdMap = HashMap<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>, ChatUserIpAddr>()
    private val idUserMap = HashMap<ChatUserIpAddr, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()

    private fun createNewUserServer(channel: AsynchronousSocketChannel): AsyncServer<PeerToPeerMsg, PeerToPeerMsg> {        // creating session - async. single connection server for exact one user
        // creating session - async. single connection server for exact one user
        return AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToPeerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = this
        )
    }

    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("New connection established with peer!")
        users.add(createNewUserServer(channel))
    }


    override fun messageReceived(msg: PeerToPeerMsg,
                                 attachment: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        when (msg.msgType!!) {
            PeerToPeerMsg.Type.CONNECT -> {
                if (attachment in userIdMap) {
                    throw ProtocolViolation("Client is already connected [protocol violation]")
                }
                userIdMap[attachment] = msg.userFromId!!
                idUserMap[msg.userFromId!!] = attachment
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
        users.remove(conn)
        idUserMap.remove(userIdMap[conn])
        userIdMap.remove(conn)
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
        users.forEach { destroyConnection(it) }
        userIdMap.clear()
        idUserMap.clear()
    }

    fun getOneUserConnection(userId: ChatUserIpAddr): AsyncServer<PeerToPeerMsg, PeerToPeerMsg>? {
        return idUserMap[userId]
    }

    /**
     * Initiates new user-user session
     *
     * @param userId id of the user, with which we want to initiate new session
     * @param conn channel, representing user-user connection
     */
    fun initiateNewUserConnection(userId: ChatUserIpAddr,
                                  conn: AsynchronousSocketChannel): AsyncServer<PeerToPeerMsg, PeerToPeerMsg> {
        val userServer = createNewUserServer(conn)
        userServer.writeMessage(p2pConnectMsg(currentClientId))
        userIdMap[userServer] = userId
        idUserMap[userId] = userServer
        return userServer
    }

    /**
     * Checks if client sent CONNECT message already; This check is performed
     * before any other user interaction with server
     */
    private fun checkClientConnected(client: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>): ChatUserIpAddr {
        if (client !in userIdMap) {
            throw ProtocolViolation("Bad client (session not properly started)")
        }
        return userIdMap[client]!!
    }
}
