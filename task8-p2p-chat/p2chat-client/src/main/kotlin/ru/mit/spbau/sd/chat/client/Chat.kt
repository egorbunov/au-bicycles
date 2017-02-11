package ru.mit.spbau.sd.chat.client

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.model.ChatModel
import ru.mit.spbau.sd.chat.client.net.*
import ru.mit.spbau.sd.chat.commons.net.AsyncConnectionAcceptor
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.channels.AsynchronousServerSocketChannel

/**
 * Class, which provides high-level interface to client side of the chat
 *
 * @param serverAddress chat server address
 * @param clientInfo this chat user information
 */
class Chat(serverAddress: SocketAddress, clientInfo: ChatUserInfo) {
    companion object {
        val logger = LoggerFactory.getLogger(Chat::class.java.name)
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


        val chatServerService = ChatServerService(serverAddress, clientId, clientInfo)
        sessionController = UsersSessionsController(clientId)
        val usersConnectionCreator = UserConnectionCreator(sessionController, sessionController)
        val usersConnectionsInterface = UsersConnectionsInterface(sessionController, usersConnectionCreator)
        val networkShield = ChatNetworkShield(clientId, chatServerService, usersConnectionsInterface)
        chatModel = ChatModel(clientId, clientInfo)

        // main class
        controller = ChatController(networkShield, chatModel)

        // wiring controller as listener for special events
        networkShield.addClientLifecycleListener(controller)
        sessionController.addUsersEventHandler(controller)

        usersConnectionAcceptor = AsyncConnectionAcceptor(usersConnectionsAcceptingSocket, usersConnectionCreator)
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
        controller.stopClient()
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
     * Returns list of users stored in chat model
     */
    fun getAvailableUsersList(): List<Pair<ChatUserIpAddr, ChatUserInfo>> {
        return chatModel.getUsers()
    }
}
