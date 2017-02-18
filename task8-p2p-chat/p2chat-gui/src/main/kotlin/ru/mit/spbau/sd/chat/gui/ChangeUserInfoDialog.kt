package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.BorderPane
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import tornadofx.*

/**
 * User personal data change dialog
 *
 * @param userId id of the user (ip addr)
 * @param userInfo user details
 */
class ChangeUserInfoDialog(
        userInfo: ChatUserInfo,
        userId: ChatUserIpAddr,
        private val controller: ChatUIController) : View() {

    override val root = BorderPane()
    val userProp = SimpleStringProperty(userInfo.name!!)

    init {
        title = "Change client profile"

        val btnSave = button("Save") {
            setOnAction {
                controller.newUserName(createChatUserInfo(userProp.get()))
                closeModal()
            }
            disableProperty().bind(userProp.isEmpty)
        }

        with(root) {
            center {
                setOnKeyPressed {
                    if (it.code == KeyCode.ENTER && !btnSave.isDisabled) {
                        controller.newUserName(createChatUserInfo(userProp.get()))
                        closeModal()
                    }
                }

                form {
                    prefHeight = 200.0
                    prefWidth = 300.0
                    fieldset("Personal Information") {
                        field("Name") {
                            textfield().bind(userProp)
                        }
                        field("Peer address") {
                            textfield {
                                isEditable = false
                                text = "${userId.ip}:${userId.port}"
                            }
                        }
                    }
                    add(btnSave)
                }
            }
        }
    }
}
