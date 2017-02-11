package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.msg.ClientLifecycleListener
import ru.mit.spbau.sd.chat.commons.p2pIAmGoneOfflineMsg
import ru.mit.spbau.sd.chat.commons.p2pIAmOnlineMsg
import ru.mit.spbau.sd.chat.commons.p2pMyInfoChangedMsg
import ru.mit.spbau.sd.chat.commons.p2pTextMessageMsg
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.util.*

/**
 * ChatNetworkInterface implementation
 *
 * @param clientId - this client id; we need it to protect client from connecting to itself
 */
internal open class ChatNetworkShield(
        private val clientId: ChatUserIpAddr,
        private val chatServerService: ChatServerService,
        private val usersConnectionsInterface: UsersConnectionsInterface) :
        ChatNetworkInterface {

    companion object {
        val logger = LoggerFactory.getLogger(UsersSessionsController::class.java)!!
    }

    private val listeners = ArrayList<ClientLifecycleListener>()

    /**
     * @param listener instance, which will be notified about completion
     *        of any client lifecycle specific operation
     */
    fun addClientLifecycleListener(listener: ClientLifecycleListener) {
        listeners.add(listener)
    }

    fun removeClientLifecycleListener(listener: ClientLifecycleListener) {
        listeners.remove(listener)
    }

    /**
     * Sending "I'am online" message to server and to
     */
    override fun startClient(clientInfo: ChatUserInfo) {
        val usersList = chatServerService.startChatting().get()
        for ((userId) in usersList.filter { it.first != clientId }) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmOnlineMsg(clientInfo))
                        usersConnectionsInterface.disconnectUser(server)
                    },
                    onFail = {
                        logger.error("User $userId connect failed: $it")
                    }
            )
        }
        listeners.forEach { it.clientStarted(usersList) }
    }

    override fun stopClient() {
        chatServerService.stopChatting()
        val users = chatServerService.getUsers().get()
        for ((userId) in users.filter { it.first != clientId }) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmGoneOfflineMsg())
                        usersConnectionsInterface.disconnectUser(server)
                    },
                    onFail = {
                        logger.error("User $userId connect failed: $it")
                    }
            )
        }
        listeners.forEach { it.clientStopped() }
    }

    override fun sendChatMessage(userId: ChatUserIpAddr, msg: ChatMessage) {
        if (userId == clientId) {
            return
        }
        usersConnectionsInterface.connectToUser(
                userId,
                onComplete = { conn ->
                    conn.writeMessage(p2pTextMessageMsg(msg))
                },
                onFail = {
                    logger.error("User $userId connect failed: $it")
                }
        )
    }

    override fun changeClientInfo(newInfo: ChatUserInfo) {
        chatServerService.changeClientInfo(newInfo)
        val users = chatServerService.getUsers().get()
        for ((userId) in users.filter { it.first != clientId }) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pMyInfoChangedMsg(newInfo))
                        usersConnectionsInterface.disconnectUser(server)
                    },
                    onFail = {
                        logger.error("User $userId connect failed: $it")
                    }
            )
        }
    }
}
