package ru.mit.spbau.sd.chat.gui

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.layout.BorderPane
import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.createChatMessage
import ru.mit.spbau.sd.chat.client.model.ChatEventsListener
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import tornadofx.*
import java.awt.Toolkit
import java.text.SimpleDateFormat
import java.util.*

/**
 * View class for messages items
 */
data class ChatMsgView(val author: String, val msg: ChatMessage) {
    override fun toString(): String {
        return "[${SimpleDateFormat("hh-mm-ss").format(Date(msg.timestamp))}] $author: ${msg.text}"
    }
}

/**
 * View class for user item
 *
 * @param name user name
 * @param id user id for identification through controller
 * @param hasNotReadMessage flag, if true ==> there are not read messages!
 */
data class ChatUserView(val name: String, val id: ChatUserIpAddr, val hasNotReadMessage: Boolean = false) {
    override fun toString(): String {
        return name + (if (hasNotReadMessage) " *" else "")
    }
}

/**
 * That is Main Window of chat application; It is based on BorderPane.
 */
class MainWindow : View("Chat"), ChatEventsListener<ChatUserIpAddr> {
    companion object {
        val logger = LoggerFactory.getLogger("ChatClientGUI")!!
    }

    override val root = BorderPane()
    private val messageFieldText = SimpleStringProperty()
    private val controller = ChatUIController(this)

    // chat users list
    private val usersList = FXCollections.observableArrayList<ChatUserView>()!!
    private val usersListView = listview(usersList)


    // chat messages list
    private val messageList = FXCollections.observableArrayList<ChatMsgView>()
    private val messageListView = ListView<ChatMsgView>(messageList)

    private var activeUser: ChatUserIpAddr? = null


    init {
        usersListView.setOnMouseClicked {
            logger.debug(usersListView.selectionModel.selectedIndices.toString())
            val item = usersListView.selectionModel.selectedItems[0]
            if (item != null) {
                newDialogChosen(item)
            }
        }


        val btnSend = button("Send") {
            prefWidth = 100.0
            minWidth = 70.0
            setOnAction { sendMessageBtnPressed() }
            disableProperty().bind(messageFieldText.isEmpty)
        }


        with(root) {
            top {
                menubar {
                    menu("File") {
                        menuitem("Change server") {
                            val p = controller.getServerPeerAddr()
                            val d = ChooseServerDialog(p.first, p.second, controller)
                            d.openModal()
                        }
                        menuitem("User profile") {
                            val d = ChangeUserInfoDialog(
                                    controller.getMyInfo(),
                                    controller.getMyId(),
                                    controller)
                            d.openModal()
                        }
                    }
                }
            }
            center {
                setOnKeyPressed { if (it.code == KeyCode.ENTER && !btnSend.isDisabled) sendMessageBtnPressed() }
                borderpane {
                    center {
                        listview(messageList) { }
                    }
                    bottom {
                        hbox(5.0) {
                            paddingAll = 10.0
                            alignment = Pos.CENTER
                            textfield {
                                bind(messageFieldText)
                                prefWidth = 600.0
                            }
                            add(btnSend)
                        }
                    }
                }
            }
            right {
                add(usersListView)
            }
        }
    }

    private fun sendMessageBtnPressed() {
        val message = messageFieldText.get()
        if (activeUser == null) {
            alert(Alert.AlertType.WARNING, "Info", "No chat selected")
            return
        }
        messageFieldText.set("")
        controller.sendMessage(activeUser!!, createChatMessage(message))
    }

    private fun newDialogChosen(user: ChatUserView) {
        logger.debug("New currently chosen user: ${user.name}")

        activeUser = user.id
        runAsync {
            controller.getUserMessages(user.id)
        } ui { messages ->
            changeUserInList(ChatUserView(user.name, user.id, false))
            messageList.clear()
            val sz = Math.min(50, messages.size)
            messages.takeLast(sz).forEach {
                messageList.add(ChatMsgView(
                        if (it.first == user.id && user.id != controller.getMyId()) user.name else "You",
                        it.second
                ))
            }
            messageListView.scrollTo(sz - 2)
            messageListView.refresh()
        }
    }

    /*
        Unfortunately every chat event handler must be inside `Platform.runLater`, because
        chat works asynchronously and may execute them in non FX thread
     */

    override fun userBecomeOnline(userId: ChatUserIpAddr, userInfo: ChatUserInfo) {
        Platform.runLater {
            val userView = if (userId == controller.getMyId()) {
                ChatUserView(userInfo.name + " [ YOU ]", userId, false)
            } else {
                ChatUserView(userInfo.name, userId, false)
            }
            logger.debug("Adding user view $userView to user list")
            usersList.add(userView)
        }
    }

    override fun userGoneOffline(userId: ChatUserIpAddr) {
        Platform.runLater {
            logger.debug("Removing user with id $userId from list")
            usersList.remove(usersList.find { it.id == userId })
            if (userId == activeUser) {
                activeUser = null
                messageList.clear()
            }
        }
    }

    override fun userChanged(userId: ChatUserIpAddr, newInfo: ChatUserInfo) {
        Platform.runLater {
            val oldUser = usersList.find { it.id == userId }
            logger.debug("User changed; Changing user ${oldUser!!.name} to ${newInfo.name}")

            changeUserInList(ChatUserView(newInfo.name, userId, oldUser.hasNotReadMessage))
        }
    }

    override fun messageReceived(senderId: ChatUserIpAddr, message: ChatMessage) {
        Platform.runLater {
            Toolkit.getDefaultToolkit().beep()
            logger.debug("Message received...")
            val user = usersList.find { it.id == senderId }
            if (activeUser == null || senderId != activeUser) {
                logger.debug("Mariking user ${user!!.name} as having new message")
                changeUserInList(ChatUserView(user.name, senderId, true))
            } else if (activeUser != null && senderId == activeUser) {
                logger.debug("Adding new message [${message.text}] to current message list")
                val newMsgView = ChatMsgView(user!!.name, message)
                messageList.add(ChatMsgView(user.name, message))
                messageListView.scrollTo(newMsgView)
                messageListView.refresh()


                // TODO: remove if too much
            }
        }
    }

    override fun messageSent(recipientId: ChatUserIpAddr, message: ChatMessage) {
        Platform.runLater {
            logger.debug("Message sent by us; Adding to current view, if needed...")
            if (activeUser == recipientId) {
                val newMsgView = ChatMsgView("You", message)
                messageList.add(ChatMsgView("You", message))
                messageListView.scrollTo(newMsgView)
                messageListView.refresh()
            }
        }
    }

    override fun currentClientInfoChanged(newInfo: ChatUserInfo) {
        Platform.runLater {
            logger.debug("This client info changed;")
            changeUserInList(newUserView = ChatUserView(
                    newInfo.name + " [ YOU ]",
                    controller.getMyId(),
                    usersList.find { it.id == controller.getMyId() }!!.hasNotReadMessage))
        }
    }

    // change user in list to the new one, but with the same id
    private fun changeUserInList(newUserView: ChatUserView) {
        var oldIdx = -1
        usersList.forEachIndexed { i, chatUserView ->
            if (chatUserView.id == newUserView.id) {
                oldIdx = i
            }
        }
        if (oldIdx < 0) {
            throw IllegalStateException("Bad user index")
        }

        usersList.add(oldIdx, newUserView)
        usersList.removeAt(oldIdx + 1)
        usersListView.refresh()
    }
}
