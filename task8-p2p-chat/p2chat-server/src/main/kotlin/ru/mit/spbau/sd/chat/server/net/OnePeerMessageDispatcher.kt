package ru.mit.spbau.sd.chat.server.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.listToUsersList
import ru.mit.spbau.sd.chat.commons.net.AsyncChannelServer
import ru.mit.spbau.sd.chat.commons.net.MessageListener
import ru.mit.spbau.sd.chat.commons.net.createStartReadingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import ru.mit.spbau.sd.chat.commons.net.state.MessageRead
import ru.mit.spbau.sd.chat.commons.net.state.NothingToWrite
import ru.mit.spbau.sd.chat.commons.net.state.ReadingState
import ru.mit.spbau.sd.chat.commons.net.state.WritingState
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.net.InetSocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler


/**
 * Peer requests dispatcher
 *
 * @param msgListener object, which handles all messages from peers except disconnect message
 * @param disconnectListener object, which handles disconnect peer event
 */
internal class OnePeerMessageDispatcher(
        val msgListener: PeerMsgListener<ChatUserIpAddr>,
        val disconnectListener: PeerDisconnectListener<AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>>
): MessageListener<PeerToServerMsg, AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>> {

    /**
     * @param msg received message
     * @param attachment channel server, which received this message
     */
    override fun messageReceived(msg: PeerToServerMsg,
                                 attachment: AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>) {
        when (msg.msgType!!) {
            PeerToServerMsg.Type.PEER_ONLINE ->
                msgListener.peerBecomeOnline(msg.userId!!, msg.userInfo!!)
            PeerToServerMsg.Type.DISCONNECT ->
                disconnectListener.peerDisconnected(attachment)
            PeerToServerMsg.Type.GET_AVAILABLE_USERS -> {
                val availableUsers = listToUsersList(msgListener.usersRequested())
                val usersMessage = ServerToPeerMsg.newBuilder()
                        .setMsgType(ServerToPeerMsg.Type.AVAILABLE_USERS)
                        .setUsers(availableUsers)
                        .build()!!
                attachment.writeMessage(usersMessage)
            }
            PeerToServerMsg.Type.MY_INFO_CHANGED ->
                msgListener.peerChangedInfo(msg.userId!!, msg.userInfo!!)
            PeerToServerMsg.Type.PEER_GONE_OFFLINE ->
                msgListener.peerGoneOffline(msg.userId!!)
            PeerToServerMsg.Type.UNRECOGNIZED -> {
                logger.error("Bad [peer->server] message type!")
            }
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(OnePeerMessageDispatcher::class.java)!!
    }
}
