package ru.mit.spbau.sd.chat.gui

import ru.spbau.mit.sd.commons.proto.ChatUserInfo

fun createChatUserInfo(name: String): ChatUserInfo {
    return ChatUserInfo.newBuilder().setName(name).build()!!
}