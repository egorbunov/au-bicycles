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
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Interface to all chat client connections to other users
 */
internal class UsersConnectionManager(
        private val clientId: ChatUserIpAddr,
        private val sessionsController: UsersSessionsController
): AsyncConnectionListener {

    private val logger =
            LoggerFactory.getLogger("UsersConnManager[${clientId.port}]")!!

    /**
     * Because of undefined order of incoming/out-coming connections we need
     * to synchronize somehow because we wan't to establish 2 connections with
     * 2 exactly same users. For this reasons reason we use this hash map of locks,
     * which will work like a charm in case user A tries to connect to US and
     * WE trying to connect to A at the same time
     */
    private val locks = ConcurrentHashMap<ChatUserIpAddr, ReentrantLock>()

    private fun getLock(userId: ChatUserIpAddr): ReentrantLock {
        return locks.getOrPut(userId, { ReentrantLock() })
    }

    /**
     * Synchronously establishes connection with user specified by given `userId`.
     *
     * Connection may actually already be established, in such case completion handler
     * `onComplete` will probably be evaluated faster (it will be evaluated in current
     * thread and as part of the call to this method)
     */
    fun connectToUser(userId: ChatUserIpAddr): AsyncServer<PeerToPeerMsg, PeerToPeerMsg, ChatUserIpAddr> {
        val l = getLock(userId)

        l.lock()
        try {
            val srv = sessionsController.getOneUserConnection(userId)
            if (srv != null) {
                return srv
            }
            val address = InetSocketAddress(InetAddress.getByName(userId.ip), userId.port)
            val channel = asyncConnect(address).get(10, TimeUnit.SECONDS)
            logger.debug("Writing CONNECT message...")
            asyncWrite(channel, createStartWritingState(p2pConnectMsg(clientId).toByteArray())).get()
            logger.debug("Waiting for CONNECT_OK...")
            val msg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
            if (msg.msgType != PeerToPeerMsg.Type.CONNECT_OK) {
                throw IllegalStateException("CONNECT OK expected")
            }
            logger.debug("Got CONNECT_OK! Creating new session!")
            sessionsController.addPreparedConnection(userId, channel, UserSessionType.ESTABLISHED_BY_ME)
            return sessionsController.getOneUserConnection(userId)!!
        } finally {
            l.unlock()
        }
    }

    /**
     * Handles new user connection, sets up it correctly and gave to session controller
     * to start new user-user session
     */
    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("Got new user connected, waiting for CONNECT message ===>")
        val msg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
        if (msg.msgType != PeerToPeerMsg.Type.CONNECT) {
            throw IllegalStateException("CONNECT expected from channel")
        }
        val userId = msg.userFromId!!
        logger.debug("===> Got CONNECT message from user = ${userId.port}")

        val srv = sessionsController.getOneUserConnection(userId)
        if (srv != null) {
            logger.debug("Client already has connection with that user")
            channel.close()
            return
        }

        val lock = getLock(userId)

        logger.debug("Locking locks[${userId.port}] -> $lock")
        lock.lock()
        logger.debug("[INCOMING CONNECTION] Acquired locks[${userId.port}]")
        try {
            val conn = sessionsController.getOneUserConnection(userId)
            if (conn != null) {
                // we have lost the game!
                logger.debug("Client already has connection with that user! =)")
                channel.close()
                return
            }
            logger.debug("[INCOMING CONNECTION] Writing connect ok...")
            asyncWrite(channel, createStartWritingState(p2pConnectOkMsg().toByteArray())).get()

            sessionsController.addPreparedConnection(userId, channel, UserSessionType.ESTABLISHED_REMOTELY)
        } finally {
            lock.unlock()
        }
    }

    /**
     * Enforces to destroy particular connection with one peer,
     * so next attempt to call `connectToUser(userId)` will result
     * in new connection establishment
     *
     * @param userId id of the user to destroy connection with
     */
    fun disconnectUser(userId: ChatUserIpAddr) {
        val lock = getLock(userId)
        lock.lock()
        try {
            sessionsController.destroyConnection(userId)
        } finally {
            // TODO: racy place =)))
            locks.remove(userId)
            lock.unlock()
        }
    }
}
