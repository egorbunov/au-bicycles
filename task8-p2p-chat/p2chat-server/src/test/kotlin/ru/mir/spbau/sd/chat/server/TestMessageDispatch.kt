package ru.mir.spbau.sd.chat.server

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.*
import ru.mit.spbau.sd.chat.commons.net.AsyncServer
import ru.mit.spbau.sd.chat.server.net.PeerEventHandler
import ru.mit.spbau.sd.chat.server.net.PeersSessionController
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg


class TestMessageDispatch {
    @Test
    fun testMessageDispatch() {
        val msgListener: PeerEventHandler<ChatUserIpAddr> = mock()
        val dispatcher = PeersSessionController(msgListener)
        val userId = ChatUserIpAddr.newBuilder().setIp("1.1.1.1").setPort(42).build()!!
        val attachment: AsyncServer<PeerToServerMsg, ServerToPeerMsg> = mock {
            onGeneric { writeMessage(any()) } doThrow RuntimeException()
            on { destroy() } doThrow RuntimeException()
        }

        dispatcher.messageReceived(p2sConnectMsg(userId), attachment)

        try {
            dispatcher.messageReceived(p2sAvailableUsersRequestMsg(), attachment)
        } catch (e: Exception) {
        }
        verify(msgListener).usersRequested()

        val userInfo = ChatUserInfo.newBuilder().setName("George").build()
        dispatcher.messageReceived(p2sMyInfoChangedMsg(userInfo), attachment)
        verify(msgListener).peerChangedInfo(userId = userId, newInfo = userInfo)

        dispatcher.messageReceived(p2sPeerGoneOfflineMsg(), attachment)
        verify(msgListener).peerGoneOffline(userId = userId)

        dispatcher.messageReceived(p2sPeerOnlineMsg(userInfo), attachment)
        verify(msgListener).peerBecomeOnline(userId = userId, userInfo = userInfo)

        try {
            dispatcher.messageReceived(p2sDisconnectMsg(), attachment)
        } catch (e: RuntimeException) {}
        verify(attachment).destroy()
    }
}
