package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.net.*
import ru.mit.spbau.sd.chat.commons.p2pConnectMsg
import ru.mit.spbau.sd.chat.commons.p2pConnectOkMsg
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * Interface to all chat client connections to other users
 */
internal class UsersConnectionManager(
        private val clientId: ChatUserIpAddr,
        private val sessionsController: UsersSessionsController
): AsyncConnectionListener {

    companion object {
        val logger = LoggerFactory.getLogger(UsersConnectionManager::class.java.simpleName)!!
    }


    private val locks = ConcurrentHashMap<ChatUserIpAddr, ReentrantLock>()

    /**
     * Asynchronously establishes connection with user specified by given `userId`.
     *
     * Connection may actually already be established, in such case completion handler
     * `onComplete` will probably be evaluated faster (it will be evaluated in current
     * thread and as part of the call to this method)
     */
    fun connectToUser(userId: ChatUserIpAddr,
                      onComplete: (AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr>) -> Unit,
                      onFail: (Throwable?) -> Unit) {
        val srv = sessionsController.getOneUserConnection(userId)
        if (srv != null) {
            onComplete(srv)
            return
        }

        asyncConnect(
                InetSocketAddress(InetAddress.getByName(userId.ip), userId.port),
                onComplete = { channel ->
                    val lock = locks.getOrPut(userId, { ReentrantLock() })
                    logger.debug("Async Connected! Now competing: $lock")
                    lock.lock()
                    logger.debug("Acquired lock (in handler)!")
                    try {
                        val conn = sessionsController.getOneUserConnection(userId)
                        if (conn != null) {
                            // we've lost the locks game =)
                            channel.close()
                            onComplete(conn)
                            return@asyncConnect
                        }
                        asyncWrite(channel, createStartWritingState(p2pConnectMsg(clientId).toByteArray())).get()
                        val msg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
                        if (msg.msgType != PeerToPeerMsg.Type.CONNECT_OK) {
                            throw IllegalStateException("CONNECT OK expected")
                        }
                        sessionsController.addPreparedConnection(userId, channel, UserSessionType.ESTABLISHED_BY_ME)
                        onComplete(sessionsController.getOneUserConnection(userId)!!)
                    } catch (e: Throwable) {
                        onFail(e)
                    } finally {
                        lock.unlock()
                        locks.remove(userId)
                    }
                },
                onFail = {
                    logger.error("Failed to connect async to ${userId.ip}:${userId.port}")
                    onFail(it)
                }
        )
    }

    /**
     * Handles new user connection, sets up it correctly and gave to session controller
     * to start new user-user session
     */
    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("Got new user connected, waiting from him his ID..")
        val msg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
        if (msg.msgType != PeerToPeerMsg.Type.CONNECT) {
            throw IllegalStateException("CONNECT Expected from channel")
        }
        val userId = msg.userFromId!!
        logger.debug("Get CONNECT signal; User id = ${userId.ip}:${userId.port}")

        val srv = sessionsController.getOneUserConnection(userId)
        if (srv != null) {
            logger.debug("Client already has connection with that user")
            channel.close()
            return
        }

        val lock = locks.getOrPut(userId, { ReentrantLock() })
        logger.debug("Got ip; Now competing for session creation: $lock")
        lock.lock()
        logger.debug("Acquired lock (in connection established method)!")
        try {
            val conn = sessionsController.getOneUserConnection(userId)
            if (conn != null) {
                // we have lost the game!
                logger.debug("Client already has connection with that user! =)")
                channel.close()
                return
            }
            asyncWrite(channel, createStartWritingState(p2pConnectOkMsg().toByteArray())).get()
            sessionsController.addPreparedConnection(userId, channel, UserSessionType.ESTABLISHED_REMOTELY)
        } finally {
            lock.unlock()
            locks.remove(userId)
        }
    }

    fun disconnectUser(userId: ChatUserIpAddr) {
        sessionsController.destroyConnection(userId)
    }
}
