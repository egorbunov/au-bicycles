package ru.mir.spbau.sd.chat.server

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.P2SMessageConstructor
import ru.mit.spbau.sd.chat.commons.net.AsyncChannelServer
import ru.mit.spbau.sd.chat.server.net.OnePeerMessageDispatcher
import ru.mit.spbau.sd.chat.server.net.PeerDisconnectListener
import ru.mit.spbau.sd.chat.server.net.PeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import ru.spbau.mit.sd.commons.proto.PeerToServerMsg
import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg


class PeerMsgDispatcherTest {
    @Test
    fun testDisconnectDispatched() {
        val disconnectListener: PeerDisconnectListener<AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg>>
                = mock()

        val dispatcher = OnePeerMessageDispatcher(mock(), disconnectListener)
        val p2sMsgBuilder = P2SMessageConstructor("1.1.1.1", 42)
        val attachment: AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg> = mock()

        dispatcher.messageReceived(p2sMsgBuilder.disconnectMsg(), attachment)
        verify(disconnectListener).peerDisconnected(attachment)
    }

    @Test
    fun testOtherMessagesDispatched() {
        val msgListener: PeerMsgListener<ChatUserIpAddr> = mock()
        val dispatcher = OnePeerMessageDispatcher(msgListener, mock())
        val p2sMsgBuilder = P2SMessageConstructor("1.1.1.1", 42)
        val attachment: AsyncChannelServer<PeerToServerMsg, ServerToPeerMsg> = mock {
            onGeneric { writeMessage(any()) } doThrow RuntimeException()
        }

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
    }
}
