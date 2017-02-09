package ru.mit.spbau.sd.chat.commons

import ru.spbau.mit.sd.commons.proto.*

/**
 * Peer to Server message constructor
 */
class P2SMessageConstructor(val userId: ChatUserIpAddr) {
    /**
     * @param ip ip address (without port) string
     * @param port port, at which user listens for other users connections
     */
    constructor(ip: String, port: Int) : this(ChatUserIpAddr.newBuilder()
            .setIp(ip).setPort(port).build())

    /**
     * Returns constructed peer to server message, which stands, that
     * peer with given identifier (ip, port) is now online and available
     * for conversation
     */
    fun peerOnlineMsg(userInfo: ChatUserInfo): PeerToServerMsg {
        return PeerToServerMsg.newBuilder()
                .setMsgType(PeerToServerMsg.Type.PEER_ONLINE)
                .setUserId(userId)
                .setUserInfo(userInfo)
                .build()!!
    }

    /**
     * Returns constructed Peer to Server message standing for that
     * peer, supplied in message body, is no longer available
     */
    fun peerGoneOfflineMsg(): PeerToServerMsg {
        return PeerToServerMsg.newBuilder()
                .setMsgType(PeerToServerMsg.Type.PEER_GONE_OFFLINE)
                .setUserId(userId)
                .build()!!
    }

    /**
     * Returns message, which is used as request for available users list from
     * server
     */
    fun availableUsersRequestMsg(): PeerToServerMsg {
        return PeerToServerMsg.newBuilder()
                .setMsgType(PeerToServerMsg.Type.GET_AVAILABLE_USERS)
                .setUserId(userId)
                .build()
    }

    /**
     * Creates message, which tells server that peer's info has changed
     */
    fun myInfoChangedMsg(info: ChatUserInfo): PeerToServerMsg {
        return PeerToServerMsg.newBuilder()
                .setMsgType(PeerToServerMsg.Type.MY_INFO_CHANGED)
                .setUserId(userId)
                .setUserInfo(info)
                .build()
    }

    /**
     * Disconnect message
     */
    fun disconnectMsg(): PeerToServerMsg {
        return PeerToServerMsg.newBuilder()
                .setMsgType(PeerToServerMsg.Type.DISCONNECT)
                .setUserId(userId)
                .build()
    }
}


/**
 * Peer to Peer message constructor
 * @param userId id for user, which will send constructed message
 */
class P2PMessageConstructor(val userId: ChatUserIpAddr) {
    /**
     * @param ip ip address (without port) string
     * @param port port, at which user listens for other users connections
     */
    constructor(ip: String, port: Int) : this(ChatUserIpAddr.newBuilder()
            .setIp(ip).setPort(port).build())

    /**
     * Returns constructed peer to peer message, which stands, that
     * peer with given identifier (ip, port) is now online and available
     * for conversation
     */
    fun iAmOnlineMsg(): PeerToPeerMsg {
        return PeerToPeerMsg.newBuilder()
                .setMsgType(PeerToPeerMsg.Type.I_AM_ONLINE)
                .setUserFromId(userId)
                .build()!!
    }

    /**
     * Returns constructed Peer to Peer message standing for that
     * peer, supplied in message body, is no longer available
     */
    fun iAmGoneOfflineMsg(userInfo: ChatUserInfo): PeerToPeerMsg {
        return PeerToPeerMsg.newBuilder()
                .setMsgType(PeerToPeerMsg.Type.I_AM_GONE_OFFLINE)
                .setUserFromId(userId)
                .setUserInfo(userInfo)
                .build()!!
    }

    /**
     * Message, which contains text message to other peer
     */
    fun textMessageMsg(msg: ChatMessage): PeerToPeerMsg {
        return PeerToPeerMsg.newBuilder()
                .setMsgType(PeerToPeerMsg.Type.TEXT_MESSAGE)
                .setUserFromId(userId)
                .setMessage(msg)
                .build()
    }

    /**
     * Creates message, which tells to other peer that other
     * peer's info has changed
     */
    fun myInfoChangedMsg(info: ChatUserInfo): PeerToPeerMsg {
        return PeerToPeerMsg.newBuilder()
                .setMsgType(PeerToPeerMsg.Type.MY_INFO_CHANGED)
                .setUserFromId(userId)
                .setUserInfo(info)
                .build()
    }

    /**
     * Disconnect message
     */
    fun disconnectMsg(): PeerToPeerMsg {
        return PeerToPeerMsg.newBuilder()
                .setMsgType(PeerToPeerMsg.Type.DISCONNECT)
                .setUserFromId(userId)
                .build()
    }
}
