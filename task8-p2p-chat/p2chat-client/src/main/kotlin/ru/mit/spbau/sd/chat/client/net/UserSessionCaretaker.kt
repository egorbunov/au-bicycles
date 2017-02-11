package ru.mit.spbau.sd.chat.client.net

import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg

/**
 * Interface implemented by class, which takes care of user sessions
 */
interface UserSessionCaretaker {
    /**
     * Take care of connection, which was established from remote user
     */
    fun setupRemotelyInitiatedConnection(userConn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>)

    /**
     * Take care of connection, which was established by current chat client
     *
     * @param userId - id of user, with whom this connection was established
     */
    fun setupThisClientInitiatedConnection(userId: ChatUserIpAddr, userConn: AsyncServer<PeerToPeerMsg, PeerToPeerMsg>)
}
