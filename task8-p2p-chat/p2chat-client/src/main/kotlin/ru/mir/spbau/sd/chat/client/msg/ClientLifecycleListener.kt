package ru.mir.spbau.sd.chat.client.msg

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr


interface ClientLifecycleListener {
    fun clientStarted(usersList: List<Pair<ChatUserIpAddr, ChatUserInfo>>)
    fun clientStopped()
    fun clientChangedInfo(newInfo: ChatUserInfo)
}