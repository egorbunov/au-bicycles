package ru.mit.spbau.sd.chat.client.net

import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr

/**
 * Class, which is used to encapsulate chat client
 * initialization routine
 */
interface IChatBootsrapper {

    /**
     * Performs registration in the p2p chat and returns initial list of users
     * available for chatting
     */
    fun registerInDaChat(userId: ChatUserIpAddr, userInfo: ChatUserInfo)
            : List<Pair<ChatUserIpAddr, ChatUserInfo>>

    /**
     * Returns peer id, which is used for bootstraping (peer server)
     */
    fun peerServerId(): ChatUserIpAddr
}

