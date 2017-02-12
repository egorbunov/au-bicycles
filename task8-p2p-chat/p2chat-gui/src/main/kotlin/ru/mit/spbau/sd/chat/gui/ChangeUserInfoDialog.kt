package ru.mit.spbau.sd.chat.gui

import javafx.beans.property.SimpleStringProperty
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import tornadofx.*

/**
 * User personal data change dialog
 */
class ChangeUserInfoDialog(userInfo: ChatUserInfo): Fragment() {
    override val root = Form()
    val userProp = SimpleStringProperty(userInfo.name!!)

    init {
        title = "Change client profile"

        with (root) {
            fieldset("Personal Information") {
                field("Name") {
                    textfield().bind(userProp)
                }
            }

            button("Save") {
                setOnAction {
                    fire(ThisClientInfoChangedEvent(createChatUserInfo(userProp.get())))
                    closeModal()
                }
                disableProperty().bind(userProp.isNull)
            }
        }
    }
}