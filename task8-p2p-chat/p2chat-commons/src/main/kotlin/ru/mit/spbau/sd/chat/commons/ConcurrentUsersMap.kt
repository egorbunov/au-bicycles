package ru.mit.spbau.sd.chat.commons

import ru.mit.spbau.sd.chat.commons.UsersMap
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.User
import ru.spbau.mit.sd.commons.proto.UsersList
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

/**
 * Users map, which uses concurrent hash map for storing users
 */
class ConcurrentUsersMap : UsersMap<InetSocketAddress> {
    private val usersMap = ConcurrentHashMap<InetSocketAddress, User>()

    override fun getUsers(): UsersList {
        return UsersList.newBuilder()
                .addAllUsers(usersMap.values)
                .build()
    }

    override fun addUser(userId: InetSocketAddress, userInfo: ChatUserInfo) {
        if (userId in usersMap) {
            throw IllegalArgumentException("User is already added in active list =( [protocol violation]")
        }
        usersMap.put(userId, buildUser(userId, userInfo))
    }

    override fun removeUser(userId: InetSocketAddress) {
        if (userId !in usersMap) {
            throw IllegalArgumentException("Can't remove non-existing user [protocol violation]")
        }
        usersMap.remove(userId)
    }

    override fun editUser(userId: InetSocketAddress, newUserInfo: ChatUserInfo) {
        if (userId !in usersMap) {
            throw IllegalArgumentException("Can't edit non-existing user [protocol violation]")
        }

        usersMap.put(userId, buildUser(userId, newUserInfo))
    }

    private fun buildUser(userId: InetSocketAddress, userInfo: ChatUserInfo): User {
        return User.newBuilder()
                .setId(
                        ChatUserIpAddr.newBuilder()
                                .setIp(userId.address.hostAddress!!)
                                .setPort(userId.port)
                                .build()
                )
                .setInfo(userInfo)
                .build()!!
    }
}
