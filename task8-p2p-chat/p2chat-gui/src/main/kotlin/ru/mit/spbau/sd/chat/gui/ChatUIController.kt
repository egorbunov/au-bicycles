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


class ChatUIController(val mainWindow: MainWindow,
                       var userInfo: ChatUserInfo = createChatUserInfo("CHANGE_NAME")) :
        Controller() {

    companion object {
        val logger = LoggerFactory.getLogger("ChatUIController")!!
    }

    @Volatile
    var chatBackend: Chat? = null


    var serverAddr: InetSocketAddress? = null


    init {
        FX.primaryStage.setOnCloseRequest {
            logger.debug("Shutting down client...")
            if (chatBackend != null) {
                chatBackend!!.stopClient()
            }
        }

        subscribe<NewServerChosenEvent> {
            try {
                serverAddr = InetSocketAddress(it.host, it.port)
            } catch (e: Exception) {
                alert(Alert.AlertType.ERROR, "Client error", "Can't start client with specified server addr")
                return@subscribe
            }
            runAsync {
                if (chatBackend != null) {
                    chatBackend!!.stopClient()
                }
                chatBackend = Chat(serverAddr!!, userInfo)
                try {
                    chatBackend!!.addChatEventListener(mainWindow)
                    chatBackend!!.startClient()
                } catch (e: Exception) {
                    chatBackend = null
                    Platform.runLater {
                        alert(Alert.AlertType.ERROR, "Client error", "Can't start client with specified server")
                    }
                }
            }
        }

        subscribe<ThisClientInfoChangedEvent> {
            userInfo = it.newInfo
            if (chatBackend != null) {
                chatBackend!!.changeClientInfo(userInfo)
            }
        }

    }

    fun getUserMessages(userId: ChatUserIpAddr): List<Pair<ChatUserIpAddr, ChatMessage>> {
        if (chatBackend == null) {
            return emptyList()
        }
        return chatBackend!!.getMessagesWithUser(userId)
    }

    fun sendMessage(userId: ChatUserIpAddr, message: ChatMessage) {
        if (chatBackend != null) {
            chatBackend!!.sendTextMessage(userId, message)
        }
    }

    fun getMyId(): ChatUserIpAddr? {
        if (chatBackend == null) {
            return null
        } else {
            return chatBackend!!.getMyId()
        }
    }

    fun getMyInfo(): ChatUserInfo {
        return userInfo
    }
}