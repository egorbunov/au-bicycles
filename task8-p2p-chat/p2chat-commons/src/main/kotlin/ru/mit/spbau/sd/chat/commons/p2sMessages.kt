package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.*

// =========== peer to server messages ==============

/**
 * Connect message; This message is sent just after connection is
 * established, every time.
 */
fun p2sConnectMsg(userId: ChatUserIpAddr): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.CONNECT)
            .setUserId(userId)
            .build()
}

/**
 * Returns constructed peer to server message, which stands, that
 * peer with given identifier (ip, port) is now online and available
 * for conversation
 */
fun p2sPeerOnlineMsg(userInfo: ChatUserInfo): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.PEER_ONLINE)
            .setUserInfo(userInfo)
            .build()!!
}

/**
 * Returns constructed Peer to Server message standing for that
 * peer, supplied in message body, is no longer available
 */
fun p2sPeerGoneOfflineMsg(): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.PEER_GONE_OFFLINE)
            .build()!!
}

/**
 * Returns message, which is used as request for available users list from
 * server
 */
fun p2sAvailableUsersRequestMsg(): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.GET_AVAILABLE_USERS)
            .build()
}

/**
 * Creates message, which tells server that peer's info has changed
 */
fun p2sMyInfoChangedMsg(info: ChatUserInfo): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.MY_INFO_CHANGED)
            .setUserInfo(info)
            .build()
}

/**
 * Disconnect message
 */
fun p2sDisconnectMsg(): PeerToServerMsg {
    return PeerToServerMsg.newBuilder()
            .setMsgType(PeerToServerMsg.Type.DISCONNECT)
            .build()
}
