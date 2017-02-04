package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.createStartReadingState
import ru.mit.spbau.sd.chat.commons.createStartWritingState
import ru.mit.spbau.sd.chat.commons.net.MessageRead
import ru.mit.spbau.sd.chat.commons.net.NothingToWrite
import ru.mit.spbau.sd.chat.commons.net.ReadingState
import ru.mit.spbau.sd.chat.commons.net.WritingState
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler

/**
 * Asynchronous dialogue with one peer.
 *
 * To setup initial IO handlers one must call `setup()` and after work
 * is done `destroy()` call is expected for proper resource release.
 *
 * Every such one peer server has it's owner for proper resource release
 * handling: it is assumed, that msgProcessor knows how to correctly handle
 * server destruction (i.e. connection close). That is needed because
 * one peer connection may be closed either by getting "disconnect" signal
 * from other side of connection, or due to manual (by user) connection
 * accepting server destruction, so...
 */
internal class OnePeerServer(val channel: AsynchronousSocketChannel,
                             val msgProcessor: PeerMsgProcessor<OnePeerServer>) {

    companion object {
        val logger = LoggerFactory.getLogger(OnePeerServer::class.java)!!
    }

    private var readingState = createReadingState()
    private var writingState: WritingState = NothingToWrite()


    /**
     * Sets everything up for proper work with remote peer.
     * One must call this method for proper work with peer connection.
     */
    fun start() {
        channel.read(readingState.getBuffer(), null, object : CompletionHandler<Int, Nothing?> {
            override fun completed(result: Int?, attachment: Nothing?) {
                readingState = readingState.proceed()
                if (readingState is MessageRead<PeerToServerMsg>) {
                    val message = readingState.getMessage()
                    logger.debug("Got message from peer: $message")
                    dispatchMessage(message)
                    readingState = createReadingState()
                }
                // subscribing again (in both cases of read and not fully read message)
                channel.read(readingState.getBuffer(), null, this)
            }

            override fun failed(exc: Throwable?, attachment: Nothing?) {
                logger.error("Failed to complete async read: $exc")
            }

        })
    }

    /**
     * Closes connection with peer and releases other resources
     */
    fun destroy() {
        channel.close()
    }

    /**
     * Sends message to remote peer.
     */
    fun sendMessage(msg: ServerToPeerMsg) {
        if (writingState !is NothingToWrite) {
            throw IllegalStateException("Bad writing state of peer connection on write")
        }
        // initializing new writing state with appropriate buffer
        writingState = createStartWritingState(msg.toByteArray())
        channel.write(writingState.getBuffer(), null, object : CompletionHandler<Int, Nothing?> {

            override fun completed(result: Int?, attachment: Nothing?) {
                writingState = writingState.proceed()
                if (writingState is NothingToWrite) {
                    logger.debug("Whole message written to channel")
                } else {
                    // subscribing on write again
                    channel.write(writingState.getBuffer(), null, this)
                }
            }

            override fun failed(exc: Throwable?, attachment: Nothing?) {
                logger.error("Failed to complete async. write: $exc")
            }
        })
    }

    private fun dispatchMessage(message: PeerToServerMsg) {
        when (message.msgType!!) {
            PeerToServerMsg.Type.PEER_ONLINE ->
                msgProcessor.peerBecomeOnline(this, message.userInfo!!)
            PeerToServerMsg.Type.DISCONNECT ->
                msgProcessor.peerDisconnected(this)
            PeerToServerMsg.Type.GET_AVAILABLE_USERS -> {
                val availableUsers = msgProcessor.usersRequested()
                val usersMessage = ServerToPeerMsg.newBuilder()
                        .setMsgType(ServerToPeerMsg.Type.AVAILABLE_USERS)
                        .setUsers(availableUsers)
                        .build()!!
                sendMessage(usersMessage)
            }
            PeerToServerMsg.Type.MY_INFO_CHANGED ->
                msgProcessor.peerChangedInfo(this, message.userInfo!!)
            PeerToServerMsg.Type.PEER_GONE_OFFLINE ->
                msgProcessor.peerGoneOffline(this)
            PeerToServerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->server] message type!")
            }
        }
    }

    /**
     * Creates initial reading state (initial means state at the beginning
     * of read operation)
     */
    private fun createReadingState(): ReadingState<PeerToServerMsg> {
        return createStartReadingState { bytes ->
            PeerToServerMsg.parseFrom(bytes)
        }
    }
}
