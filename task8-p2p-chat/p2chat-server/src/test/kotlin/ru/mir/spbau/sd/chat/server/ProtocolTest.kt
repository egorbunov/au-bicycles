package ru.mir.spbau.sd.chat.server

import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.junit.After
import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.*
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg

class ProtocolTest {
    private val sessionController = PeersSessionController(mock())
    private val userId = ChatUserIpAddr.newBuilder().setIp("1.1.1.1").setPort(42).build()!!
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
        sessionController.messageReceived(p2sConnectMsg(userId), attachment)
        Assert.assertEquals(
                sessionController.getOneUserConnection(userId),
                attachment
        )

        try {
            sessionController.messageReceived(p2sDisconnectMsg(), attachment)
        } catch (e: RuntimeException) {}
        Assert.assertNull(
                sessionController.getOneUserConnection(userId)
        )
    }

    @Test(expected = ProtocolViolation::class)
    fun testDisconnectProtocolViolation() {
        sessionController.messageReceived(p2sDisconnectMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testGetUsersProtocolViolation() {
        sessionController.messageReceived(p2sAvailableUsersRequestMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerOnlineProtocolViolation() {
        sessionController.messageReceived(p2sPeerOnlineMsg(userInfo), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerOfflineProtocolViolation() {
        sessionController.messageReceived(p2sPeerGoneOfflineMsg(), attachment)
    }

    @Test(expected = ProtocolViolation::class)
    fun testPeerChangedInfoProtocolViolation() {
        sessionController.messageReceived(p2sMyInfoChangedMsg(userInfo), attachment)
    }
}