package ru.mir.spbau.sd.chat.client.net

import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.commons.net.asyncConnect
import ru.mit.spbau.sd.chat.commons.userIpToSockAddr
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel

/**
 * Interface to all chat client connections to other users
 */
internal class UsersConnectionsInterface(private val sessionsController: UsersSessionsController) {

    /**
     * Asynchronously performs operation on connection, after establishing this connection
     */
    fun connectAndPerform(userId: ChatUserIpAddr,
                          onComplete: (AsyncServer<PeerToPeerMsg, PeerToPeerMsg>) -> Unit,
                          onFail: (Throwable?) -> Unit) {
        val userConnection = sessionsController.getOneUserConnection(userId)
        if (userConnection != null) {
            onComplete(userConnection)
            return
        }

        asyncConnect(
                userIpToSockAddr(userId),
                onComplete = { channel: AsynchronousSocketChannel ->
                    val userServer = sessionsController.initiateNewUserConnection(userId, channel)
                    onComplete(userServer)
                },
                onFail = onFail)
    }
}
