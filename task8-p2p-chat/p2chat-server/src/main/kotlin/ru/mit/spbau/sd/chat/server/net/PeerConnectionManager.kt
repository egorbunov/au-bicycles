package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.net.*
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel

/**
 * This class ensures that connection with peer is established properly
 */
class PeerConnectionManager internal constructor(
        val sessionController: PeersSessionController
): AsyncConnectionListener {
    companion object {
        val logger = LoggerFactory.getLogger(PeerConnectionManager::class.java.simpleName)!!
    }

    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        asyncRead(
                channel,
                createStartReadingState { PeerToServerMsg.parseFrom(it) },
                onComplete = { msg ->
                    if (msg.msgType != PeerToServerMsg.Type.CONNECT) {
                        logger.error("CONNECT message expected from peer, got: $msg")
                        return@asyncRead
                    }
                    val userId = msg.userId!!
                    asyncWrite(
                            channel,
                            createStartWritingState(ServerToPeerMsg.newBuilder()
                                    .setMsgType(ServerToPeerMsg.Type.CONNECT_OK)
                                    .build()
                                    .toByteArray()!!))
                            .get()
                    logger.info("Establish connection with peer: ${userId.ip}:${userId.port}")
                    sessionController.addPreparedConnection(channel, userId)
                },
                onFail = {
                    logger.error("Async read failed: $it")
                })
    }
}
