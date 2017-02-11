package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.ChatUserAlreadyExists
import ru.mit.spbau.sd.chat.client.msg.UsersNetEventHandler
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.commons.net.MessageListener
import ru.mit.spbau.sd.chat.commons.p2pConnectMsg
import ru.mit.spbau.sd.chat.commons.p2pDisconnectMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.util.*

/**
 * Holder and controller of all client connections to other clients
 */
internal class UsersSessionsController(
        val currentClientId: ChatUserIpAddr
) :
        MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>,
        UserSessionCaretaker {

    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java)!!
    }
    private val users = ArrayList<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()
    private val userIdMap: MutableMap<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>, ChatUserIpAddr> = HashMap()
    private val idUserMap: MutableMap<ChatUserIpAddr, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>> = HashMap()
    private val usersEventHandlers = ArrayList<UsersNetEventHandler<ChatUserIpAddr>>()

    fun addUsersEventHandler(handler: UsersNetEventHandler<ChatUserIpAddr>) {
        usersEventHandlers.add(handler)
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
                usersEventHandlers.forEach { it.userBecomeOnline(peerId, msg.userInfo!!) }
            }
            PeerToPeerMsg.Type.MY_INFO_CHANGED -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandlers.forEach { it.userChangeInfo(peerId, msg.userInfo!!) }
            }
            PeerToPeerMsg.Type.I_AM_GONE_OFFLINE -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandlers.forEach { it.userGoneOffline(peerId) }
            }
            PeerToPeerMsg.Type.TEXT_MESSAGE -> {
                val peerId = checkClientConnected(attachment)
                usersEventHandlers.forEach { it.userSentMessage(peerId, msg.message!!) }
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
                onComplete = {
                    logger.error("Destroying connection...")
                    conn.destroy()
                },
                onFail = { e->
                    logger.error("Failed to destroy user connection [disconnect msg. sending failed]: $e")
                })
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

    fun getAllUsersConnections(): List<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>> {
        return users
    }

    /**
     * Destroys connection with user specified by it's id, if connection exists
     */
    fun destroyConnection(userId: ChatUserIpAddr) {
        if (userId !in idUserMap) {
            return
        }
        destroyConnection(idUserMap[userId]!!)
    }

    override fun setupThisClientInitiatedConnection(userId: ChatUserIpAddr,
                                                    userConn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        if (userConn in users) {
            throw IllegalStateException("Can't add user session twice")
        }
        if (userId in idUserMap) {
            throw IllegalStateException("Session for user $userId already exists")
        }
        userConn.startReading()
        userConn.writeMessage(p2pConnectMsg(currentClientId))
        users.add(userConn)
        userIdMap[userConn] = userId
        idUserMap[userId] = userConn
    }

    override fun setupRemotelyInitiatedConnection(userConn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        if (userConn in users) {
            throw IllegalStateException("Can't add user session twice")
        }
        userConn.startReading()
        users.add(userConn)
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
