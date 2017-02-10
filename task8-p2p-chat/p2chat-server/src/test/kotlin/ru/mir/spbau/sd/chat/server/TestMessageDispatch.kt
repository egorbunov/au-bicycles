package ru.mir.spbau.sd.chat.server

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.P2SMessageConstructor
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
        val p2sMsgBuilder = P2SMessageConstructor("1.1.1.1", 42)
        val attachment: AsyncServer<PeerToServerMsg, ServerToPeerMsg> = mock {
            onGeneric { writeMessage(any()) } doThrow RuntimeException()
            on { destroy() } doThrow RuntimeException()
        }

        dispatcher.messageReceived(p2sMsgBuilder.connectMsg(), attachment)

        try {
            dispatcher.messageReceived(p2sMsgBuilder.availableUsersRequestMsg(), attachment)
        } catch (e: Exception) {
        }
        verify(msgListener).usersRequested()

        val userInfo = ChatUserInfo.newBuilder().setName("George").build()
        dispatcher.messageReceived(p2sMsgBuilder.myInfoChangedMsg(userInfo), attachment)
        verify(msgListener).peerChangedInfo(userId = p2sMsgBuilder.userId, newInfo = userInfo)

        dispatcher.messageReceived(p2sMsgBuilder.peerGoneOfflineMsg(), attachment)
        verify(msgListener).peerGoneOffline(userId = p2sMsgBuilder.userId)

        dispatcher.messageReceived(p2sMsgBuilder.peerOnlineMsg(userInfo), attachment)
        verify(msgListener).peerBecomeOnline(userId = p2sMsgBuilder.userId, userInfo = userInfo)

        try {
            dispatcher.messageReceived(p2sMsgBuilder.disconnectMsg(), attachment)
        } catch (e: RuntimeException) {}
        verify(attachment).destroy()
    }
}
