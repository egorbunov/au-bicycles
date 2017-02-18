package ru.mit.spbau.sd.chat.client

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr

fun createClient(ip: String, port: Int): ChatUserIpAddr {
    return ChatUserIpAddr.newBuilder()
            .setIp(ip)
            .setPort(port)
            .build()!!
}

fun createInfo(name: String): ChatUserInfo {
    return ChatUserInfo.newBuilder()
            .setName(name)
            .build()!!
}
