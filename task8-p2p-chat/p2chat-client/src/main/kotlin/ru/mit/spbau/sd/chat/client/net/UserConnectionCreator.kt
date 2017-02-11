package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.net.*
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel

/**
 * That is listener of remotely established connections and also
 * interface for creating connections, initiated by current chat client
 */
class UserConnectionCreator(
        val connectionCaretaker: UserSessionCaretaker,
        val connMessageListener: MessageListener<PeerToPeerMsg, AsyncServer<PeerToPeerMsg, PeerToPeerMsg>>
): AsyncConnectionListener {

    companion object {
        val logger = LoggerFactory.getLogger(UserConnectionCreator::class.java)!!
    }

    private fun createServer(channel: AsynchronousSocketChannel): AsyncServer<PeerToPeerMsg, PeerToPeerMsg> {
        return AsyncServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToPeerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = connMessageListener
        )
    }
    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("New remote connection established: ${channel.remoteAddress}")
        val conn = createServer(channel)
        connectionCaretaker.setupRemotelyInitiatedConnection(conn)
    }

    /**
     * @param userId id of the user, with which connection was established
     */
    fun initiateConnectionByThisClient(userId: ChatUserIpAddr, channel: AsynchronousSocketChannel):
            AsyncServer<PeerToPeerMsg, PeerToPeerMsg> {
        val conn = createServer(channel)
        connectionCaretaker.setupThisClientInitiatedConnection(userId, conn)
        return conn
    }
}