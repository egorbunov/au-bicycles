package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.User
import ru.spbau.mit.sd.commons.proto.UsersList
import java.net.InetAddress
import java.net.InetSocketAddress


fun intSizeInBytes(): Int {
    return 4
}

/**
 * Converts given inet socket address to protobuf `ChatUserIpAddr` struct
 */
fun inetSockAddrToUserIp(addr: InetSocketAddress): ChatUserIpAddr {
    return ChatUserIpAddr.newBuilder()
            .setIp(addr.address.hostAddress!!)
            .setPort(addr.port)
            .build()!!
}

/**
 * Converts protobuf `ChatUserIpAddr` to socket address
 */
fun userIpToSockAddr(userIp: ChatUserIpAddr): InetSocketAddress {
    return InetSocketAddress(InetAddress.getByName(userIp.ip), userIp.port)
}

/**
 * Builds UsersList message, which can be simply sent through socket, from
 * java list of users descriptions
 */
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

/**
 * Expands UsersList message as java list
 */
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
