package ru.mit.spbau.sd.chat.client.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr

/**
 * This bootstrapper is used in case peer has no peer server and so
 * it is server to itself
 *
 * @param userId id of this user
 */
class DummyChatBootstrapper(private val userId: ChatUserIpAddr): IChatBootsrapper {
    override fun peerServerId(): ChatUserIpAddr {
        return userId
    }

    override fun registerInDaChat(userId: ChatUserIpAddr, userInfo: ChatUserInfo)
            : List<Pair<ChatUserIpAddr, ChatUserInfo>> {
        return listOf(Pair(userId, userInfo))
    }
}