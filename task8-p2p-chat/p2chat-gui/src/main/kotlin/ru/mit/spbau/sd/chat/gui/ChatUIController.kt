package ru.mit.spbau.sd.chat.gui

import javafx.application.Platform
import javafx.scene.control.Alert
import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.Chat
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import tornadofx.Controller
import tornadofx.FX
import tornadofx.alert
import java.net.InetSocketAddress

/**
 * Class, which wires UI and "Network" part of the chat
 */
class ChatUIController(val mainWindow: MainWindow,
                       var userInfo: ChatUserInfo = createChatUserInfo("CHANGE_NAME")) :
        Controller() {

    companion object {
        val logger = LoggerFactory.getLogger("ChatUIController")!!
    }

    @Volatile
    private var chatBackend: Chat = Chat(userInfo)
    private var peerServerAddress: InetSocketAddress? = null


    init {
        FX.primaryStage.setOnCloseRequest {
            logger.debug("Shutting down client...")
            chatBackend.stopClient()
        }

        /**
         * Listening to peer-server update events from UI
         */
//        subscribe<NewPeerServerChosenEvent> {
//            try {
//                peerServerAddress = InetSocketAddress(it.host, it.port)
//                chatBackend.stopClient()
//                chatBackend = Chat(userInfo, peerServerAddress)
//                startClientBackend()
//            } catch (e: Exception) {
//                fallbackToDefaultPeerServer()
//                alert(Alert.AlertType.ERROR, "Client error", "Can't start client with specified server addr")
//            }
//        }

        /**
         * Listening to client name change event from UI
         */
//        subscribe<ThisClientInfoChangedEvent> {
//            userInfo = it.newInfo
//            chatBackend.changeClientInfo(userInfo)
//        }

        startClientBackend()
    }

    private fun fallbackToDefaultPeerServer() {
        chatBackend = Chat(userInfo)
        startClientBackend()
    }

    private fun startClientBackend() {
        try {
            chatBackend.addChatEventListener(mainWindow)
            chatBackend.startClient()
        } catch (e: Exception) {
            if (chatBackend.serverPeerAddress != null) {
                fallbackToDefaultPeerServer()
            }
            Platform.runLater {
                alert(Alert.AlertType.ERROR, "Client error", "Can't start client with specified server")
            }
        }
    }

    fun getUserMessages(userId: ChatUserIpAddr): List<Pair<ChatUserIpAddr, ChatMessage>> {
        return chatBackend.getMessagesWithUser(userId)
    }

    /**
     * Send message through network to other chat user
     *
     * @param userId id of the user-recipient
     * @param message text chat message
     */
    fun sendMessage(userId: ChatUserIpAddr, message: ChatMessage) {
        chatBackend.sendTextMessage(userId, message)
    }

    /**
     * Returns this client id (ip addr)
     */
    fun getMyId(): ChatUserIpAddr = chatBackend.getMyId()

    /**
     * Returns this client user details
     */
    fun getMyInfo(): ChatUserInfo = userInfo

    fun getServerPeerAddr(): Pair<String, Int> {
        if (chatBackend.serverPeerAddress == null) {
            return Pair(chatBackend.getMyId().ip, chatBackend.getMyId().port)
        } else {
            val a = chatBackend.serverPeerAddress as InetSocketAddress
            return Pair(a.hostString, a.port)
        }
    }

    /**
     * Listening to peer-server update events from UI
     */
    fun newPeerServerChosen(host: String?, port: Int) {
        try {
            peerServerAddress = InetSocketAddress(host, port)
            chatBackend.stopClient()
            chatBackend = Chat(userInfo, peerServerAddress)
            startClientBackend()
        } catch (e: Exception) {
            fallbackToDefaultPeerServer()
            alert(Alert.AlertType.ERROR, "Client error", "Can't start client with specified server addr")
        }
    }

    /**
     * Listening to client name change event from UI
     */
    fun newUserName(newInfo: ChatUserInfo) {
        userInfo = newInfo
        chatBackend.changeClientInfo(userInfo)
    }
}
