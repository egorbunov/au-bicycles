package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.*
import ru.mit.spbau.sd.chat.commons.net.*
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.net.SocketAddress
import java.nio.channels.AsynchronousSocketChannel
import java.util.concurrent.CountDownLatch

/**
 * Class, which provides interface to chat server.
 * On each method it will create separate client-server connection.
 *
 * @param serverAddress - address of the server, which will be queried
 * @param clientId - current client id, to sent to server
 * @param clientUserInfo - current client info
 */
class ChatServerService(
        val serverAddress: SocketAddress,
        private val clientId: ChatUserIpAddr,
        private var clientUserInfo: ChatUserInfo) {

    companion object {
        val logger = LoggerFactory.getLogger(ChatServerService::class.java.simpleName)!!
    }


    private val usersListCountdown = CountDownLatch(1)
    @Volatile
    private var lastUsersList: List<Pair<ChatUserIpAddr, ChatUserInfo>>? = null


    /**
     * Connection to server and tells it, that this client is online.
     * After it requests list of users, already available for chatting
     *
     * @return future, promising you the list of available chat users at the moment of
     *         current client connection
     */
    fun startChatting(): AsyncFuture<List<Pair<ChatUserIpAddr, ChatUserInfo>>> {
        val channel = asyncConnect(serverAddress).get()
        sendMsg(channel, p2sConnectMsg(clientId))
        sendMsg(channel, p2sPeerOnlineMsg(clientUserInfo))
        sendMsg(channel, p2sAvailableUsersRequestMsg())
        return recvUsersAndCloseFuture(channel)
    }

    /**
     * Returns future of users list
     */
    fun getUsers(): AsyncFuture<List<Pair<ChatUserIpAddr, ChatUserInfo>>> {
        val channel = asyncConnect(serverAddress).get()
        sendMsg(channel, p2sConnectMsg(clientId))
        sendMsg(channel, p2sAvailableUsersRequestMsg())
        return recvUsersAndCloseFuture(channel)
    }


    /**
     * Connects to server and tells it, that this client is offline
     */
    fun stopChatting() {
        val channel = asyncConnect(serverAddress).get()
        sendMsg(channel, p2sConnectMsg(clientId))
        sendMsg(channel, p2sPeerGoneOfflineMsg())
        logger.debug("Sending disconnect to server...")
        sendMsg(channel, p2sDisconnectMsg()).get()
        channel.close()
    }

    /**
     * Sends new client info to server
     */
    fun changeClientInfo(newInfo: ChatUserInfo) {
        clientUserInfo = newInfo
        val channel = asyncConnect(serverAddress).get()
        sendMsg(channel, p2sConnectMsg(clientId))
        sendMsg(channel, p2sMyInfoChangedMsg(clientUserInfo))
        logger.debug("Sending disconnect to server...")
        sendMsg(channel, p2sDisconnectMsg()).get()
        channel.close()
    }

    private fun recvUsersAndCloseFuture(channel: AsynchronousSocketChannel):
            AsyncFuture<List<Pair<ChatUserIpAddr, ChatUserInfo>>> {
        return recvMsg(channel).thenApply { msg ->
            logger.debug("Sending disconnect to server...")
            sendMsg(channel, p2sDisconnectMsg()).get()
            channel.close()
            when (msg.msgType) {
                ServerToPeerMsg.Type.AVAILABLE_USERS -> {
                    usersListToList(msg.users!!)
                }
                ServerToPeerMsg.Type.UNRECOGNIZED -> {
                    logger.error("Bad message from server, can't recv users from server")
                    throw RuntimeException("bad server msg")
                }
            }
        }
    }

    private fun sendMsg(channel: AsynchronousSocketChannel, msg: PeerToServerMsg): AsyncFuture<Unit> {
        return asyncWrite(channel, createStartWritingState(msg.toByteArray()!!))
    }

    private fun recvMsg(channel: AsynchronousSocketChannel): AsyncFuture<ServerToPeerMsg> {
        return asyncRead(channel, createStartReadingState { ServerToPeerMsg.parseFrom(it) })
    }

}
