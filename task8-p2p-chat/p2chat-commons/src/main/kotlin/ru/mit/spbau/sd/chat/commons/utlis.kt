package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.User
import ru.spbau.mit.sd.commons.proto.UsersList
import java.net.InetAddress
import java.net.InetSocketAddress

/**
 * Created by: Egor Gorbunov
 * Date: 2/3/17
 * Email: egor-mailbox@ya.com
 */

fun intSizeInBytes(): Int {
    return 4
}

fun inetSockAddrToUserIp(addr: InetSocketAddress): ChatUserIpAddr {
    return ChatUserIpAddr.newBuilder()
            .setIp(addr.address.hostAddress!!)
            .setPort(addr.port)
            .build()!!
}

fun userIpToSockAddr(userIp: ChatUserIpAddr): InetSocketAddress {
    return InetSocketAddress(InetAddress.getByName(userIp.ip), userIp.port)
}

fun listToUsersList(list: List<Pair<ChatUserIpAddr, ChatUserInfo>>): UsersList {
    return UsersList.newBuilder()
            .addAllUsers(
            list.map { p ->
                User.newBuilder()
                        .setId(p.first)
                        .setInfo(p.second)
                        .build()!!
            })
            .build()!!
}

fun usersListToList(usersList: UsersList): List<Pair<ChatUserIpAddr, ChatUserInfo>> {
    return usersList.usersList!!.map {
        Pair(it.id!!, it.info!!)
    }
}

/**
 * Returns the string, with maximum `n` chars
 */
fun String.limit(n: Int): String {
    if (this.length < n) {
        return this
    } else {
        return this.substring(0..n)
    }
}
