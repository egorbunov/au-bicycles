package ru.mit.spbau.sd.chat.gui

import ru.spbau.mit.sd.commons.proto.ChatUserInfo

/**
 * Simply creates ChatUserInfo object from given name-string
 */
fun createChatUserInfo(name: String): ChatUserInfo {
    return ChatUserInfo.newBuilder().setName(name).build()!!
}