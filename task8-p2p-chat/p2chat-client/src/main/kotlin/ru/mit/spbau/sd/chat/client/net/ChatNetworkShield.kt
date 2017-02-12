package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.msg.ClientLifecycleListener
import ru.mit.spbau.sd.chat.commons.*
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.util.*
import java.util.concurrent.CountDownLatch

/**
 * ChatNetworkInterface implementation
 *
 * @param clientId - this client id; we need it to protect client from connecting to itself
 */
internal open class ChatNetworkShield(
        private val clientId: ChatUserIpAddr,
        private val chatServerService: ChatServerService,
        private val usersConnManager: UsersConnectionManager) :
        ChatNetworkInterface {

    val logger = LoggerFactory.getLogger("NetShield[${clientId.port}]")!!

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
    override fun startClient(clientInfo: ChatUserInfo): AsyncFuture<Unit> {
        val usersList = chatServerService.startChatting().get()
        val filteredList = usersList.filter { it.first != clientId }
        val countdown = CountDownLatch(filteredList.size)
        for ((userId) in filteredList) {
            usersConnManager.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmOnlineMsg(clientInfo))
                        usersConnManager.disconnectUser(userId)
                        countdown.countDown()
                    },
                    onFail = {
                        logger.error("User [${userId.ip}:${userId.port}] connect failed: $it")
                        countdown.countDown()
                    }
            )
        }
        listeners.forEach { it.clientStarted(usersList) }

        return object: AsyncFuture<Unit> {
            override fun get() {
                countdown.await()
            }
        }
    }

    override fun stopClient(): AsyncFuture<Unit> {
        chatServerService.stopChatting()
        val usersList = chatServerService.getUsers().get()
        val filteredList = usersList.filter { it.first != clientId }
        val countdown = CountDownLatch(filteredList.size)
        for ((userId) in filteredList) {
            usersConnManager.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pIAmGoneOfflineMsg())
                        usersConnManager.disconnectUser(userId)
                        countdown.countDown()
                    },
                    onFail = {
                        logger.error("User [${userId.ip}:${userId.port}] connect failed: $it")
                        countdown.countDown()
                    }
            )
        }
        listeners.forEach { it.clientStopped() }

        return object: AsyncFuture<Unit> {
            override fun get() {
                countdown.await()
            }
        }
    }

    override fun sendChatMessage(userId: ChatUserIpAddr, msg: ChatMessage) {
        if (userId == clientId) {
            return
        }
        usersConnManager.connectToUser(
                userId,
                onComplete = { conn ->
                    conn.writeMessage(p2pTextMessageMsg(msg))
                },
                onFail = {
                    logger.error("User [${userId.ip}:${userId.port}] connect failed: $it")
                }
        )
    }

    override fun changeClientInfo(newInfo: ChatUserInfo) {
        chatServerService.changeClientInfo(newInfo)
        val usersList = chatServerService.getUsers().get()
        val filteredList = usersList.filter { it.first != clientId }
        for ((userId) in filteredList) {
            usersConnManager.connectToUser(
                    userId,
                    onComplete = { server ->
                        server.writeMessage(p2pMyInfoChangedMsg(newInfo))
                        usersConnManager.disconnectUser(userId)
                    },
                    onFail = {
                        logger.error("User [${userId.ip}:${userId.port}] connect failed: $it")
                    }
            )
        }
    }
}
