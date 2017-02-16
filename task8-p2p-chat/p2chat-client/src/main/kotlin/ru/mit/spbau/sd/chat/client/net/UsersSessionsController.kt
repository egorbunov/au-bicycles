package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.model.ChatModelInterface
import ru.mit.spbau.sd.chat.client.msg.UsersNetEventHandler
import ru.mit.spbau.sd.chat.commons.ProtocolViolation
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.commons.net.MessageListener
import ru.mit.spbau.sd.chat.commons.net.createStartReadingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import ru.mit.spbau.sd.chat.commons.p2pDisconnectMsg
import ru.mit.spbau.sd.chat.commons.p2pRegistrationOkMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Holder and controller of all client connections to other clients.
 *
 * This class takes care of all peer2peer sessions and it receives all the messages
 * incoming from other clients to us. This messages are dispatched here too and to listen
 * to incoming messages one can subscribe using `UsersNetEventHandler`
 */
internal class UsersSessionsController(
        val currentClientId: ChatUserIpAddr,
        val chatModelInterface: ChatModelInterface<ChatUserIpAddr>) :
        MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr>>
{
    val logger = LoggerFactory.getLogger("SessionControl[${currentClientId.port}]")!!

    // all these structures may be accessed from different threads
    // because of asynchronous nature of this chat
    private val idUserMap =
            ConcurrentHashMap<ChatUserIpAddr, AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr>>()
    private val idSessioonTypeMap = ConcurrentHashMap<ChatUserIpAddr, UserSessionType>()
    private val usersEventHandlers = ArrayList<UsersNetEventHandler<ChatUserIpAddr>>()

    /**
     * Subscribes given handler for events, coming from chat network
     */
    fun addUsersEventHandler(handler: UsersNetEventHandler<ChatUserIpAddr>) {
        usersEventHandlers.add(handler)
    }

    /**
     * Message dispatching routine
     */
    override fun messageReceived(msg: PeerToPeerMsg,
                                 attachment: AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr>) {
        val userId = attachment.payload!!

        logger.info("Message received: " + msg)

        if (userId !in idUserMap.keys) {
            throw IllegalStateException("Got message from not stored connection!")
        }

        when (msg.msgType!!) {
            PeerToPeerMsg.Type.CONNECT -> {
                // all connect messages must be processed outside from this class,
                // before adding connection to session controller
                logger.error("Got connect message in controller, strange...")
            }
            PeerToPeerMsg.Type.CONNECT_OK -> {
                logger.error("Got connect ok message; Doing nothing...")
            }
            PeerToPeerMsg.Type.DISCONNECT -> {
                logger.debug("Got DISCONNECT message")
                if (idSessioonTypeMap[userId] == UserSessionType.ESTABLISHED_BY_ME) {
                    throw ProtocolViolation("Unexpected DISCONNECT msg")
                }
                destroyConnection(userId)
            }
            PeerToPeerMsg.Type.I_AM_ONLINE -> {
                usersEventHandlers.forEach { it.userBecomeOnline(userId, msg.userInfo!!) }
            }
            PeerToPeerMsg.Type.MY_INFO_CHANGED -> {
                usersEventHandlers.forEach { it.userChangeInfo(userId, msg.userInfo!!) }
            }
            PeerToPeerMsg.Type.I_AM_GONE_OFFLINE -> {
                usersEventHandlers.forEach { it.userGoneOffline(userId) }
            }
            PeerToPeerMsg.Type.TEXT_MESSAGE -> {
                usersEventHandlers.forEach { it.userSentMessage(userId, msg.message!!) }
            }
            PeerToPeerMsg.Type.REGISTRATION_OK -> {
                logger.error("Got REGISTRATION OK message during regular session. That is probably error.")
            }
            PeerToPeerMsg.Type.REGISTER -> {
                logger.debug("Got REGISTER message; Returning users list...")
                usersEventHandlers.forEach { it.userBecomeOnline(userId, msg.userInfo!!) }
                val users = chatModelInterface.getAllUsers()
                attachment.writeMessage(p2pRegistrationOkMsg(users))
            }
            PeerToPeerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->peer] message type!")
            }
        }
    }

    /**
     * Tries to close connection with user
     *
     * @param userId id to identify the user =)
     */
    fun destroyConnection(userId: ChatUserIpAddr) {
        logger.debug("Destroying connection with ${userId.port}")
        if (userId !in idUserMap.keys) {
            throw IllegalStateException("Can't destroy non existing connection with: [${userId.ip}:${userId.port}]")
        }
        val sessionType = idSessioonTypeMap.remove(userId)!!
        val session = idUserMap.remove(userId)!!
        if (sessionType == UserSessionType.ESTABLISHED_BY_ME) {
            session.writeMessageSync(p2pDisconnectMsg())
        }
        session.destroy()
        logger.debug("Destroyed! idUserMapLen = ${idUserMap.size}" )
    }

    /**
     * Destroys all connections
     */
    fun destroy() {
        idUserMap.values.forEach { it.destroy() }
        idUserMap.clear()
    }

    fun getOneUserConnection(userId: ChatUserIpAddr):
            AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr>? {
        return idUserMap[userId]
    }

    /**
     * Add new session with user;
     *
     * @param channel - channel for receiving/sending messages; it MUST be set up correctly
     *        (all initial connect procedures must be done)
     */
    fun addPreparedConnection(
            userId: ChatUserIpAddr,
            channel: AsynchronousSocketChannel,
            sessionType: UserSessionType) {
        if (userId in idUserMap) {
            throw IllegalStateException("Session with that user already exists")
        }
        val newSession = createServer(channel, userId)
        idUserMap[userId] = newSession
        idSessioonTypeMap[userId] = sessionType
        logger.debug("Starting new session with [{${userId.ip}:${userId.port}], type: $sessionType")
        newSession.startReading()
    }


    private fun createServer(channel: AsynchronousSocketChannel, userId: ChatUserIpAddr):
            AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr> {
        return AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToPeerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = this,
                payload = userId,
                serverName = "P2PSrv[${currentClientId.port}]"
        )
    }
}
