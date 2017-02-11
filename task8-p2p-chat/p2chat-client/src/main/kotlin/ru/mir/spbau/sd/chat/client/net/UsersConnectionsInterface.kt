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
     * Asynchronously establishes connection with user specified by given `userId`.
     *
     * Connection may actually already be established, in such case completion handler
     * `onComplete` will probably be evaluated faster (it will be evaluated in current
     * thread and as part of the call to this method)
     */
    fun connectToUser(userId: ChatUserIpAddr,
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
                    val userServer = sessionsController.initNewUserConnection(userId, channel)
                    onComplete(userServer)
                },
                onFail = onFail
        )
    }

    /**
     * Returns all currently established connections
     */
    fun getAllEstablishedConnections(): List<AsyncServer<PeerToPeerMsg, PeerToPeerMsg>> {
        return sessionsController.getAllUsersConnections()
    }

    /**
     * Destroys connection with user, which has given `userId`, if connection exists,
     * otherwise it does nothing.
     */
    fun disconnectUser(userId: ChatUserIpAddr) {
        sessionsController.destroyConnection(userId)
    }
}
