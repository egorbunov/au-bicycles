package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleStringProperty
import javafx.scene.input.KeyCode
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import tornadofx.*

/**
 * User personal data change dialog
 *
 * @param userId id of the user (ip addr)
 * @param userInfo user details
 */
class ChangeUserInfoDialog(userInfo: ChatUserInfo, userId: ChatUserIpAddr): Fragment() {
    override val root = Form()
    val userProp = SimpleStringProperty(userInfo.name!!)

    init {
        title = "Change client profile"

        with (root) {
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
            setOnKeyPressed { if (it.code == KeyCode.ENTER) onSubmit() }
            button("Save") {
                setOnAction {
                    onSubmit()
                }
                disableProperty().bind(userProp.isNull)
            }
        }
    }

    private fun onSubmit() {
        fire(ThisClientInfoChangedEvent(createChatUserInfo(userProp.get())))
        closeModal()
    }
}