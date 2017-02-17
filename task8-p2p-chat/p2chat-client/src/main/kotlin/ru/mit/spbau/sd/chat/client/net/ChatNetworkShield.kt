package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.model.ChatModelInterface
import ru.mit.spbau.sd.chat.client.msg.ClientLifecycleListener
import ru.mit.spbau.sd.chat.commons.p2pIAmGoneOfflineMsg
import ru.mit.spbau.sd.chat.commons.p2pIAmOnlineMsg
import ru.mit.spbau.sd.chat.commons.p2pMyInfoChangedMsg
import ru.mit.spbau.sd.chat.commons.p2pTextMessageMsg
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.io.IOException
import java.util.*

/**
 * Class, which provides access to network-dependent part of the chat application.
 * It is responsible for correct initialization of current client in p2p-chat
 * network, correct de-initialization and all that.
 *
 *
 * @param clientId - this client id; we need it to protect client from connecting to itself
 * @param usersConnManager - interface for establishing user-connections
 */
internal open class ChatNetworkShield(
        private val clientId: ChatUserIpAddr,
        private val usersConnManager: UsersConnectionManager,
        private val chatBootsrapper: IChatBootsrapper,
        private val chatModelInterface: ChatModelInterface<ChatUserIpAddr>) :

        ChatNetworkInterface {

    val logger = LoggerFactory.getLogger("NetShield[${clientId.port}]")!!

    private val listeners = ArrayList<ClientLifecycleListener>()

    /**
     * Adds new client lifecycle listener to be notified for events like
     * "client started" and "client stopped"
     *
     * @param listener instance, which will be notified about completion
     *        of any client lifecycle specific operation
     */
    fun addClientLifecycleListener(listener: ClientLifecycleListener) {
        listeners.add(listener)
    }


    /**
     * Bootstraps the client telling every online chat-user, that we
     * are now available for chatting
     */
    override fun startClient(clientInfo: ChatUserInfo) {
        val initialUsers = chatBootsrapper.registerInDaChat(clientId, clientInfo)
        // special filtering needed, because we don't need to tell server peer that we
        // are online
        val filteredList = initialUsers.filter {
            it.first != clientId && it.first != chatBootsrapper.peerServerId()
        }

        for ((userId) in filteredList) {
            val conn = usersConnManager.connectToUser(userId)
            try {
                conn.writeMessageSync(p2pIAmOnlineMsg(clientInfo))
            } catch (e: IOException) {
                logger.error("Can't write I_AM_ONLINE to client ${userId.ip}:${userId.port}: $e")
            }
//            usersConnManager.disconnectUser(userId)
        }
        listeners.forEach { it.clientStarted(initialUsers) }
    }

    /**
     * Tells to each peer, which is available from current user model,
     * that this user is now not available (offline)
     */
    override fun stopClient() {
        val usersList = chatModelInterface.getAllUsers()
        val filteredList = usersList.filter { it.first != clientId }

        for ((userId) in filteredList) {
            try {
                val conn = usersConnManager.connectToUser(userId)
                conn.writeMessageSync(p2pIAmGoneOfflineMsg())
            } catch (e: IOException) {
                logger.error("Can't write I_AM_GONE_OFFLINE to client ${userId.ip}:${userId.port}: $e")
            }
//            usersConnManager.disconnectUser(userId)
        }
        listeners.forEach { it.clientStopped() }
    }

    /**
     * Establishes connection with peer (if needed) and sends chat message to him
     */
    override fun sendChatMessage(userId: ChatUserIpAddr, msg: ChatMessage) {
        if (userId == clientId) {
            return
        }
        try {
            val conn = usersConnManager.connectToUser(userId)
            conn.writeMessage(p2pTextMessageMsg(msg))
        } catch (e: Exception) {
            logger.error("Can't send message to client ${userId.ip}:${userId.port}: $e")
        }
    }

    /**
     * Notifies every available peer (accordingly to chat model interface),
     * that this user changed it's info!
     */
    override fun changeClientInfo(newInfo: ChatUserInfo) {
        val usersList = chatModelInterface.getAllUsers()
        val filteredList = usersList.filter { it.first != clientId }
        for ((userId) in filteredList) {
            try {
                val conn = usersConnManager.connectToUser(userId)
                conn.writeMessageSync(p2pMyInfoChangedMsg(newInfo))
            } catch (e: IOException) {
                logger.error("Can't write MY_INFO_CAHNGED to client ${userId.ip}:${userId.port}: $e")
            }
//            usersConnManager.disconnectUser(userId)
        }
    }
}
