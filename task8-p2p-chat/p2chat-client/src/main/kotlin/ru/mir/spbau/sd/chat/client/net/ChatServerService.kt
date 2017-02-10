package ru.mir.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mir.spbau.sd.chat.client.msg.ClientLifecycleListener
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
 * @param clientLifecycleListener - instance, which will be notified about completion
 *        of any operation
 */
class ChatServerService(
        val serverAddress: SocketAddress,
        private val clientId: ChatUserIpAddr,
        private var clientUserInfo: ChatUserInfo,
        private val clientLifecycleListener: ClientLifecycleListener) {

    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java)!!
    }

    /**
     * Connection to server and tells it, that this client is online.
     * After it requests list of users, already available for chatting
     *
     * @return future where `clientLifecycleListener` is supplied with initial chat users list
     *         user can call `get()` on it to wait for this event to occur
     */
    fun startChating(): AsyncFuture<Unit> {
        val countdown = CountDownLatch(1)
        val conn = asyncConnect(serverAddress).get()
        val server = createAndStartServerSession(
                conn,
                object : MessageListener<ServerToPeerMsg, AsyncServer<ServerToPeerMsg, PeerToServerMsg>> {
                    override fun messageReceived(msg: ServerToPeerMsg,
                                                 attachment: AsyncServer<ServerToPeerMsg, PeerToServerMsg>) {
                        when (msg.msgType!!) {
                            ServerToPeerMsg.Type.AVAILABLE_USERS -> {
                                clientLifecycleListener.clientStarted(usersListToList(msg.users!!))
                                countdown.countDown()
                            }
                            ServerToPeerMsg.Type.UNRECOGNIZED -> {
                                logger.error("Bad server2peer msg: bad message type")
                                throw ProtocolViolation("Got bad message from server")
                            }
                        }
                        attachment.writeMessageSync(p2sDisconnectMsg())
                        attachment.destroy()
                    }
                })
        server.writeMessage(p2sConnectMsg(clientId))
        server.writeMessage(p2sPeerOnlineMsg(clientUserInfo))
        server.writeMessage(p2sAvailableUsersRequestMsg())

        return object: AsyncFuture<Unit> {
            override fun get() {
                countdown.await()
            }
        }
    }

    /**
     * Connects to server and tells it, that this client is offline
     */
    fun stopChating() {
        val conn = asyncConnect(serverAddress).get()
        val server = createAndStartServerSession(conn, UselessMessageListener())
        server.writeMessage(p2sConnectMsg(clientId))
        server.writeMessage(p2sPeerGoneOfflineMsg())
        // that is crucial for last write to be sync, because last message
        // may be put on write queue.
        server.writeMessageSync(p2sDisconnectMsg())
        server.destroy()
        clientLifecycleListener.clientStopped()
    }

    /**
     * Sends new client info to server
     */
    fun changeClientInfo(newInfo: ChatUserInfo) {
        clientUserInfo = newInfo
        val conn = asyncConnect(serverAddress).get()
        val server = createAndStartServerSession(conn, UselessMessageListener())
        server.writeMessage(p2sConnectMsg(clientId))
        server.writeMessage(p2sMyInfoChangedMsg(clientUserInfo))
        server.writeMessageSync(p2sDisconnectMsg())
        server.destroy()
        clientLifecycleListener.clientChangedInfo(clientUserInfo)
    }

    private fun createAndStartServerSession(channel: AsynchronousSocketChannel,
                                            msgListener: MessageListener<ServerToPeerMsg,
                                            AsyncServer<ServerToPeerMsg, PeerToServerMsg>>):
            AsyncServer<ServerToPeerMsg, PeerToServerMsg> {
        val session = AsyncServer(
                channel,
                createReadingState = { createStartReadingState { ServerToPeerMsg.parseFrom(it) } },
                createWritingState = { createStartWritingState(it.toByteArray()) },
                messageListener = msgListener
        )
        session.start()
        return session
    }
}
