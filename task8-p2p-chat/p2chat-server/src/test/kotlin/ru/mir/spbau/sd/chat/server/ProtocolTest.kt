package ru.mir.spbau.sd.chat.server

import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.junit.After
import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.P2SMessageConstructor
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.server.net.ProtocolViolation
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg

class ProtocolTest {
    private val sessionController = PeersSessionController(mock())
    private val p2sMsgBuilder = P2SMessageConstructor("1.1.1.1", 42)
    private val attachment: AsyncServer<PeerToServerMsg, ServerToPeerMsg> = mock() {
        on { destroy() } doThrow RuntimeException()
    }
    private val userInfo = ChatUserInfo.getDefaultInstance()

    @After
    fun after() {
        try {
            sessionController.destroy()
        } catch (e: RuntimeException) {}
    }

    @Test
    fun testConnectDisconnect() {
        sessionController.messageReceived(p2sMsgBuilder.connectMsg(), attachment)
        Assert.assertEquals(
                sessionController.getOneUserConnection(p2sMsgBuilder.userId),
                attachment
        )

        sessionController.messageReceived(p2sMsgBuilder.disconnectMsg(), attachment)
        Assert.assertNull(
                sessionController.getOneUserConnection(p2sMsgBuilder.userId)
        )
    }

    @Test(expected = ProtocolViolation::class)
    fun testDisconnectProtocolViolation() {
        sessionController.messageReceived(p2sMsgBuilder.disconnectMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testGetUsersProtocolViolation() {
        sessionController.messageReceived(p2sMsgBuilder.availableUsersRequestMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerOnlineProtocolViolation() {
        sessionController.messageReceived(p2sMsgBuilder.peerOnlineMsg(userInfo), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerOfflineProtocolViolation() {
        sessionController.messageReceived(p2sMsgBuilder.peerGoneOfflineMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerChangedInfoProtocolViolation() {
        sessionController.messageReceived(p2sMsgBuilder.myInfoChangedMsg(userInfo), attachment)
    }
}