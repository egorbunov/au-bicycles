package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * Created by: Egor Gorbunov
 * Date: 2/3/17
 * Email: egor-mailbox@ya.com
 */

fun socketAddrToId(address: InetSocketAddress): String {
    return address.toString()
}

fun ipMessageToAddr(userId: ChatUserIpAddr): InetSocketAddress {
    return InetSocketAddress(userId.ip, userId.port)
}

fun chatUserIpAddrToSockAddr(ip: ChatUserIpAddr): InetSocketAddress {
    return InetSocketAddress(InetAddress.getByName(ip.ip), ip.port)
}
