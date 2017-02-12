package ru.mit.spbau.sd.chat.gui

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import tornadofx.FX
import tornadofx.FXEvent

/**
 * Event, which is fired when user changes client name through UI
 */
class ThisClientInfoChangedEvent(val newInfo: ChatUserInfo): FXEvent()

/**
 * Event, which is fired when user changes server settings through UI
 */
class NewServerChosenEvent(val host: String, val port: Int): FXEvent()
