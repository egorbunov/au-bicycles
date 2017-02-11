package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg

/**
 * Returns constructed peer to peer message, which stands, that
 * peer with given identifier (ip, port) is now online and available
 * for conversation
 */
fun p2pIAmOnlineMsg(userInfo: ChatUserInfo): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.I_AM_ONLINE)
            .setUserInfo(userInfo)
            .build()!!
}

/**
 * Returns constructed Peer to Peer message standing for that
 * peer, supplied in message body, is no longer available
 */
fun p2pIAmGoneOfflineMsg(): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.I_AM_GONE_OFFLINE)
            .build()!!
}

/**
 * Message, which contains text message to other peer
 */
fun p2pTextMessageMsg(msg: ChatMessage): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.TEXT_MESSAGE)
            .setMessage(msg)
            .build()
}

/**
 * Creates message, which tells to other peer that other
 * peer's info has changed
 */
fun p2pMyInfoChangedMsg(info: ChatUserInfo): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.MY_INFO_CHANGED)
            .setUserInfo(info)
            .build()
}

/**
 * The very first message, which must be sent after connection established.
 * That is needed to specify user id, which is now talking to other user
 */
fun p2pConnectMsg(userId: ChatUserIpAddr): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.CONNECT)
            .setUserFromId(userId)
            .build()
}

/**
 * Disconnect message
 */
fun p2pDisconnectMsg(): PeerToPeerMsg {
    return PeerToPeerMsg.newBuilder()
            .setMsgType(PeerToPeerMsg.Type.DISCONNECT)
            .build()
}
