package ru.mit.spbau.sd.chat.client

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.model.ChatEventsListener
import ru.mit.spbau.sd.chat.client.model.ChatModel
import ru.mit.spbau.sd.chat.client.model.ChatModelInterface
import ru.mit.spbau.sd.chat.client.net.*
import ru.mit.spbau.sd.chat.commons.AsyncFuture
import ru.mit.spbau.sd.chat.commons.inetSockAddrToUserIp
import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.mit.spbau.sd.chat.commons.userIpToSockAddr
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

/**
 * Class, which provides high-level interface to client side of the chat
 *
 * @param serverPeerAddress address of the peer, which will provide
 *        knowledge about chat users to this new chat client. If serverPeerAddress
 *        is null when this chat client will be the server to itself
 *
 * @param clientInfo this chat user information
 */
class Chat(clientInfo: ChatUserInfo, val serverPeerAddress: SocketAddress? = null) {
    companion object {
        val logger = LoggerFactory.getLogger(Chat::class.java.name)!!
    }

    private val usersConnectionAcceptor: AsyncConnectionAcceptor
    private val controller: ChatController
    private val sessionController: UsersSessionsController
    private val chatModel: ChatModel<ChatUserIpAddr>

    init {
        val usersConnectionsAcceptingSocket = AsynchronousServerSocketChannel.open()
        usersConnectionsAcceptingSocket.bind(InetSocketAddress(0))
        val clientLocalAddr = usersConnectionsAcceptingSocket.localAddress as InetSocketAddress
        logger.debug("Client local address = $clientLocalAddr")
        val clientId = ChatUserIpAddr.newBuilder()
                .setIp(clientLocalAddr.address.hostAddress!!)
                .setPort(clientLocalAddr.port)
                .build()!!
        logger.debug("Client id = $clientId")


        chatModel = ChatModel(clientId, clientInfo)
        val bootstrapper = if (serverPeerAddress != null && serverPeerAddress != userIpToSockAddr(clientId)) {
            ChatBootsrapper(serverPeerAddress as InetSocketAddress)
        } else {
            DummyChatBootstrapper(clientId)
        }

        val chatModelInterface = ChatModelInterface(chatModel)
        sessionController = UsersSessionsController(clientId, chatModelInterface)
        val usersConnManager = UsersConnectionManager(clientId, sessionController)
        val networkShield = ChatNetworkShield(
                clientId,
                usersConnManager,
                bootstrapper,
                chatModelInterface
        )

        // main class
        controller = ChatController(networkShield, chatModel)

        // wiring controller as listener for special events
        networkShield.addClientLifecycleListener(controller)
        sessionController.addUsersEventHandler(controller)

        usersConnectionAcceptor = AsyncConnectionAcceptor(usersConnectionsAcceptingSocket, usersConnManager)
    }

    fun addChatEventListener(listener: ChatEventsListener<ChatUserIpAddr>) {
        controller.addChatModelChangeListener(listener)
    }

    /**
     * This call is propagated to `networkShield`
     */
    fun startClient() {
        logger.debug("Starting chat client...")
        controller.startClient()
        usersConnectionAcceptor.start()
    }

    /**
     * This call is propagated to `networkShield` too.
     */
    fun stopClient() {
        logger.debug("Destroying chat client...")
        usersConnectionAcceptor.destroy()
        controller.stopClient() // TODO: what bout async future return?
        sessionController.destroy()
    }

    /**
     * Send chat message to other chat client
     */
    fun sendTextMessage(userId: ChatUserIpAddr, message: ChatMessage) {
        controller.sendTextMessage(userId, message)
    }

    /**
     * Change current client info
     */
    fun changeClientInfo(newInfo: ChatUserInfo) {
        controller.changeClientInfo(newInfo)
    }

    /**
     * Returns this client info
     */
    fun getMyInfo(): ChatUserInfo {
        return chatModel.clientInfo
    }

    fun getMyId(): ChatUserIpAddr {
        return chatModel.clientId
    }

    /**
     * Returns list of messages for recipient/sender
     */
    fun getMessagesWithUser(userId: ChatUserIpAddr): List<Pair<ChatUserIpAddr, ChatMessage>> {
        return chatModel.getMessages(userId)
    }

    /**
     * Returns this client address, at which it listening for other peers
     */
    fun getAddress(): SocketAddress {
        return usersConnectionAcceptor.getAddress()
    }
}
