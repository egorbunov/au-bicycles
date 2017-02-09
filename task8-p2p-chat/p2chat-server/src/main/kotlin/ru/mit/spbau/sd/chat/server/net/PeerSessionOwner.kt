package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.net.AsyncChannelServer
import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionListener
import ru.mit.spbau.sd.chat.commons.net.createStartReadingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel
import java.util.*


/**
 * Class, which listens for asynchronously created connections.
 * For each established connection it creates a proper connection server, which
 * will propagate all read messages (coming from peer) to `peerMsgListener`
 *
 * @param peerMsgListener class, which will be notified in case of new read
 *        from channel messages
 */
internal class PeerSessionOwner(
        val peerMsgListener: PeerMsgListener<ChatUserIpAddr>
):
        AsyncConnectionListener,
        PeerDisconnectListener<AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>> {

    companion object {
        val logger = LoggerFactory.getLogger(PeerSessionOwner::class.java)!!
    }
    private val peers = ArrayList<AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>>()

    override fun connectionEstablished(channel: AsynchronousSocketChannel) {
        logger.debug("New connection established with peer!")
        // creating session - async. single connection server for exact one peer
        val newPeer = AsyncChannelServer(
                channel = channel,
                createReadingState = { createStartReadingState { PeerToServerMsg.parseFrom(it) }},
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = OnePeerMessageDispatcher(peerMsgListener, this)
        )
        peers.add(newPeer)
    }


    override fun peerDisconnected(peer: AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>) {
        peer.destroy()
    }

    fun destroy() {
        for (peer in peers) {
            peer.destroy()
        }
    }
}