package ru.mir.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mir.spbau.sd.chat.client.msg.ClientLifecycleListener
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
 */
internal class ChatNetworkShield(
        private val clientId: ChatUserIpAddr,
        private var clientInfo: ChatUserInfo,
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
    override fun startClient() {
        val usersList = chatServerService.startChatting().get()
        for ((userId) in usersList) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmOnlineMsg(clientInfo))
                        usersConnectionsInterface.disconnectUser(userId)
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
        for ((userId) in users) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmGoneOfflineMsg())
                        usersConnectionsInterface.disconnectUser(userId)
                    },
                    onFail = {
                        logger.error("User $userId connect failed: $it")
                    }
            )
        }
        listeners.forEach { it.clientStopped() }
    }

    override fun sendChatMessage(userId: ChatUserIpAddr, msg: ChatMessage) {
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
        clientInfo = newInfo
        val users = chatServerService.getUsers().get()
        for ((userId) in users) {
            usersConnectionsInterface.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pMyInfoChangedMsg(clientInfo))
                        usersConnectionsInterface.disconnectUser(userId)
                    },
                    onFail = {
                        logger.error("User $userId connect failed: $it")
                    }
            )
        }
        listeners.forEach { it.clientChangedInfo(clientInfo) }
    }
}
