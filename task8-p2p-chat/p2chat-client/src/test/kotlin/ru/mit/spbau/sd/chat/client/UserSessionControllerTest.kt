package ru.mit.spbau.sd.chat.client

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.inOrder
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import ru.mit.spbau.sd.chat.client.msg.UsersNetEventHandler
import ru.mit.spbau.sd.chat.client.net.UsersSessionsController
import ru.mit.spbau.sd.chat.commons.*
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg


class UserSessionControllerTest {
    private fun createAsyncServerMock(): AsyncServer<PeerToPeerMsg, PeerToPeerMsg> {
        val srv: AsyncServer<PeerToPeerMsg, PeerToPeerMsg> = mock()
        Mockito.doNothing().`when`(srv).destroy()
        Mockito.doNothing().`when`(srv).startReading()
        Mockito.doNothing().`when`(srv).writeMessage(any())
        Mockito.doNothing().`when`(srv).writeMessage(any(), any(), any())
        Mockito.doNothing().`when`(srv).writeMessageSync(any())
        return srv
    }

    val clientId = createClient("1.1.1.1", 42)

    @Test
    fun remotelyInitiatedUserSessionTest() {
        val sessionController = UsersSessionsController(clientId)
        val session = createAsyncServerMock()
        sessionController.setupRemotelyInitiatedConnection(session)
        verify(session).startReading()

        Assert.assertEquals(session, sessionController.getAllUsersConnections().single())
    }

    @Test
    fun thisClientInitiatedSessionTest() {
        val sessionController = UsersSessionsController(clientId)
        val session = createAsyncServerMock()
        val remoteUserId = createClient("2.2.2.2", 4421)
        sessionController.setupThisClientInitiatedConnection(remoteUserId, session)
        inOrder(session) {
            verify(session).startReading()
            verify(session).writeMessage(p2pConnectMsg(clientId))
        }

        Assert.assertEquals(session, sessionController.getOneUserConnection(remoteUserId))
        Assert.assertEquals(session, sessionController.getAllUsersConnections().single())
    }


    @Test
    fun testSimpleEventsForAlreadyConnectedUser() {
        val sessionController = UsersSessionsController(clientId)
        val session = createAsyncServerMock()
        val remoteUserId = createClient("2.2.2.2", 4421)
        val remoteUserInfo = createInfo("Hamburger")
        sessionController.setupThisClientInitiatedConnection(remoteUserId, session)

        val userEventHandler: UsersNetEventHandler<ChatUserIpAddr> = mock()
        sessionController.addUsersEventHandler(userEventHandler)

        // user become online message
        sessionController.messageReceived(p2pIAmOnlineMsg(remoteUserInfo), session)
        verify(userEventHandler).userBecomeOnline(remoteUserId, remoteUserInfo)

        // user info changed message
        val newInfo = createInfo("Maan")
        sessionController.messageReceived(p2pMyInfoChangedMsg(newInfo), session)
        verify(userEventHandler).userChangeInfo(remoteUserId, newInfo)

        // user send message
        val message = ChatMessage.newBuilder().setText("hello").build()!!
        sessionController.messageReceived(p2pTextMessageMsg(message), session)
        verify(userEventHandler).userSentMessage(remoteUserId, message)

        // user gone offline message
        sessionController.messageReceived(p2pIAmGoneOfflineMsg(), session)
        verify(userEventHandler).userGoneOffline(remoteUserId)
    }

    @Test
    fun testConnectDisconnectMessagesForRemotelyConnectedUser() {
        val sessionController = UsersSessionsController(clientId)
        val session = createAsyncServerMock()
        val remoteUserId = createClient("2.2.2.2", 4421)
        sessionController.setupRemotelyInitiatedConnection(session)

        // test connect msg
        sessionController.messageReceived(p2pConnectMsg(remoteUserId), session)
        Assert.assertEquals(session, sessionController.getOneUserConnection(remoteUserId))

        // test disconnect msg
        sessionController.messageReceived(p2pDisconnectMsg(), session)
        Assert.assertTrue(sessionController.getAllUsersConnections().isEmpty())
    }
}
