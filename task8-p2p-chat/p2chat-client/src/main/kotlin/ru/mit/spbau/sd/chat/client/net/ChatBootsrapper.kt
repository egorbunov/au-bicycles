package ru.mit.spbau.sd.chat.client.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.client.ClientHandshakeError
import ru.mit.spbau.sd.chat.commons.*
import ru.mit.spbau.sd.chat.commons.net.*
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.net.InetSocketAddress
import java.net.SocketAddress

/**
 * Class, which is used to encapsulate chat client
 * initialization routine
 *
 * @param peerServerAddress address of server-peer
 */
class ChatBootsrapper(
        val peerServerAddress: InetSocketAddress) : IChatBootsrapper {
    override fun peerServerId(): ChatUserIpAddr {
        return inetSockAddrToUserIp(peerServerAddress)
    }

    companion object {
        private val logger = LoggerFactory.getLogger("ChatBootsrapper")!!
    }


    /**
     * Registers user in the p2p chat and returns most recent list
     * of available chat-users
     *
     * This method is blocking.
     *
     * ALARM: I want to mention, that here we assume, that nobody can user
     * current client as server during registration.
     *
     * @param userId - user id (ip address of listening for connections peer socket)
     * @param userInfo - user details
     */
    override fun registerInDaChat(userId: ChatUserIpAddr, userInfo: ChatUserInfo)
            : List<Pair<ChatUserIpAddr, ChatUserInfo>> {

        logger.debug("Connecting to server peer...")
        val channel = asyncConnect(peerServerAddress).get()
        channel.use { channel ->
            logger.debug("Handshaking with server peer...")
            asyncWrite(channel, createStartWritingState(p2pConnectMsg(userId).toByteArray())).get()
            val msg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
            if (msg.msgType != PeerToPeerMsg.Type.CONNECT_OK) {
                throw ClientHandshakeError("Not connected")
            }
            logger.debug("Sending REGISTER to server peer...")
            asyncWrite(channel, createStartWritingState(p2pRegisterMsg(userInfo).toByteArray())).get()
            val usersMsg = asyncRead(channel, createStartReadingState { PeerToPeerMsg.parseFrom(it) }).get()
            if (usersMsg.msgType != PeerToPeerMsg.Type.REGISTRATION_OK) {
                throw ClientHandshakeError("Not registered")
            }
            logger.debug("Ok, got users list: " + usersMsg.usersList)
            logger.debug("Disconnecting from server-peer...")
            asyncWrite(channel, createStartWritingState(p2pDisconnectMsg().toByteArray())).get()
            return usersListToList(usersMsg.usersList!!)
        }
    }
}
