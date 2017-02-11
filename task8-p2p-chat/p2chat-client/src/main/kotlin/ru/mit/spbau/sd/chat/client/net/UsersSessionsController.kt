package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.msg.UsersNetEventHandler
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.commons.net.MessageListener
import ru.mit.spbau.sd.chat.commons.p2pConnectMsg
import ru.mit.spbau.sd.chat.commons.p2pDisconnectMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Holder and controller of all client connections to other clients
 */
internal class UsersSessionsController(
        val currentClientId: ChatUserIpAddr
) :
        MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>,
        UserSessionCaretaker {

    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java.simpleName)!!
    }
    // all these structures may be accessed from different threads because of asynchronous nature of this chat
    private val users = ConcurrentHashMap.newKeySet<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>()
    private val userIdMap: ConcurrentHashMap<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>, ChatUserIpAddr> =
            ConcurrentHashMap()
    private val idUserMap: ConcurrentHashMap<ChatUserIpAddr, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>> =
            ConcurrentHashMap()
    private val usersEventHandlers = ArrayList<UsersNetEventHandler<ChatUserIpAddr>>()

    fun addUsersEventHandler(handler: UsersNetEventHandler<ChatUserIpAddr>) {
        usersEventHandlers.add(handler)
    }

    override fun messageReceived(msg: PeerToPeerMsg,
                                 attachment: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        when (msg.msgType!!) {
            PeerToPeerMsg.Type.CONNECT -> {
                val id = msg.userFromId!!
                logger.debug("Got CONNECT message; User id = [${id.ip}:${id.port}]")
                if (attachment in userIdMap.keys) {
                    throw ProtocolViolation("Client is already connected [protocol violation]")
                }
                if (id in idUserMap.keys) {
                    val oldUser = idUserMap[id]
                    if (oldUser != null) {
                        userIdMap.remove(oldUser)
                        users.remove(oldUser)
                    }
                }
                userIdMap[attachment] = id
                idUserMap[id] = attachment
                logger.debug("User added...")
            }
            PeerToPeerMsg.Type.DISCONNECT -> {
                logger.debug("Got DISCONNECT message")
                checkClientConnected(attachment)
                destroyConnectionByRemoteSignal(attachment)
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
        conn.destroy()
    }

    private fun destroyConnectionByRemoteSignal(conn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        users.remove(conn)
        idUserMap.remove(userIdMap[conn])
        userIdMap.remove(conn)
        conn.writeMessage(p2pDisconnectMsg(),
                onComplete = {
                    logger.debug("Destroying connection...")
                    conn.destroy()
                },
                onFail = { e->
                    logger.error("Failed to destroy user connection [disconnect msg. sending failed]: $e")
                }
        )
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
        return users.toList()
    }

    /**
     * Destroys connection with user specified by it's id, if connection exists
     */
    fun destroyConnection(userId: ChatUserIpAddr) {
        if (userId !in idUserMap.keys) {
            return
        }
        destroyConnection(idUserMap[userId]!!)
    }

    override fun setupThisClientInitiatedConnection(userId: ChatUserIpAddr,
                                                    userConn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) {
        if (userConn in users) {
            throw IllegalStateException("Can't add user session twice")
        }
        if (userId in idUserMap.keys) {
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
        if (client !in userIdMap.keys) {
            throw ProtocolViolation("Bad client (session not properly started)")
        }
        return userIdMap[client]!!
    }
}
