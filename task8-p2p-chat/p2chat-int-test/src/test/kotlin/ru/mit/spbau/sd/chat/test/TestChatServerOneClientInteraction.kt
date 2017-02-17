package ru.mit.spbau.sd.chat.test

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.internal.verification.Times
import ru.mit.spbau.sd.chat.client.Chat
import ru.mit.spbau.sd.chat.client.createChatMessage
import ru.mit.spbau.sd.chat.client.model.ChatEventsListener
import ru.mit.spbau.sd.chat.commons.inetSockAddrToUserIp
import ru.mit.spbau.sd.chat.commons.userIpToSockAddr
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.SocketAddress
import java.util.*


class TestChatServerOneClientInteraction {

    fun createClient(name: String, serverPeerAddr: SocketAddress? = null): Chat {
        val chat = Chat(ChatUserInfo.newBuilder().setName(name).build()!!, serverPeerAddr)
        return chat
    }

    @Before
    fun before() {
    }

    @After
    fun after() {
    }

    @Test
    fun testClientStart() {
        val client = createClient("George")
        val chatEventListener: ChatEventsListener<ChatUserIpAddr> = mock()
        client.addChatEventListener(chatEventListener)
        client.startClient()
        Thread.sleep(100)
        chatEventListener.userBecomeOnline(client.getMyId(), client.getMyInfo())
        client.stopClient()
        Thread.sleep(100)
        chatEventListener.userGoneOffline(client.getMyId())
    }

    @Test
    fun testOneClientChangeInfo() {
        val client = createClient("George")
        val chatEventListener: ChatEventsListener<ChatUserIpAddr> = mock()
        client.addChatEventListener(chatEventListener)
        client.startClient()
        val newInfo = ChatUserInfo.newBuilder().setName("NEWMIKE").build()
        client.changeClientInfo(newInfo)
        Thread.sleep(100)
        verify(chatEventListener).currentClientInfoChanged(newInfo)
        client.stopClient()
    }

    @Test
    fun testOneClientMessageSent() {
        val client = createClient("George")
        val chatEventListener: ChatEventsListener<ChatUserIpAddr> = mock()
        client.addChatEventListener(chatEventListener)
        client.startClient()
        Thread.sleep(100)
        val message = ChatMessage.newBuilder().setText("HELLO").build()
        client.sendTextMessage(client.getMyId(), message)
        Thread.sleep(100)
        verify(chatEventListener).messageSent(client.getMyId(), message)
        verify(chatEventListener, mode = Times(0)).messageReceived(any(), any())
        client.stopClient()
    }

    @Test
    fun testTwoClientsStartStop() {
        val c1 = createClient("Alice")
        val l1 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c1.addChatEventListener(l1)
        val c2 = createClient("Bob", c1.getAddress())
        val l2 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c2.addChatEventListener(l2)

        c1.startClient()
        c2.startClient()

        Thread.sleep(100)

        verify(l1).userBecomeOnline(c2.getMyId(), c2.getMyInfo())
        verify(l2).userBecomeOnline(c1.getMyId(), c1.getMyInfo())

        c1.stopClient()
        Thread.sleep(100)
        c2.stopClient()

        Thread.sleep(100)

        verify(l1).userGoneOffline(c2.getMyId())
        verify(l2).userGoneOffline(c1.getMyId())
    }

    @Test
    fun testTwoClientsAddressing() {
        val c1 = createClient("Alice")
        val l1 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c1.addChatEventListener(l1)
        val c2 = createClient("Bob",  userIpToSockAddr(c1.getMyId()))
        val l2 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c2.addChatEventListener(l2)

        c1.startClient()
        c2.startClient()

        Thread.sleep(100)

        verify(l1).userBecomeOnline(c2.getMyId(), c2.getMyInfo())
        verify(l2).userBecomeOnline(c1.getMyId(), c1.getMyInfo())

        c1.stopClient()
        Thread.sleep(100)
        c2.stopClient()

        Thread.sleep(100)

        verify(l1).userGoneOffline(c2.getMyId())
        verify(l2).userGoneOffline(c1.getMyId())
    }

    @Test
    fun testTwoClientsMessagesSent() {
        val c1 = createClient("Alice")
        val l1 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c1.addChatEventListener(l1)
        val c2 = createClient("Bob", c1.getAddress())
        val l2 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c2.addChatEventListener(l2)

        c1.startClient()
        c2.startClient()

        Thread.sleep(100)

        val m12 = createChatMessage("hello, c2!")
        c1.sendTextMessage(c2.getMyId(), m12)
        Thread.sleep(100)
        val m21 = createChatMessage("hello, c1 =)")
        c2.sendTextMessage(c1.getMyId(), m21)

        Thread.sleep(100)

        verify(l2).messageReceived(c1.getMyId(), m12)
        verify(l1).messageReceived(c2.getMyId(), m21)

        c1.stopClient()
        Thread.sleep(100)
        c2.stopClient()
    }

    @Test
    fun twoClientsChangeInfo() {
        val c1 = createClient("Alice")
        val l1 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c1.addChatEventListener(l1)
        val c2 = createClient("Bob", c1.getAddress())
        val l2 = mock<ChatEventsListener<ChatUserIpAddr>>()
        c2.addChatEventListener(l2)

        c1.startClient()
        c2.startClient()

        Thread.sleep(100)

        val newInfo1 = ChatUserInfo.newBuilder().setName("Egor").build()!!
        c1.changeClientInfo(newInfo1)
        val newInfo2 = ChatUserInfo.newBuilder().setName("Kate").build()!!
        c2.changeClientInfo(newInfo2)

        Thread.sleep(100)

        verify(l1).userChanged(c2.getMyId(), newInfo2)
        verify(l2).userChanged(c1.getMyId(), newInfo1)

        c1.stopClient()
        Thread.sleep(100)
        c2.stopClient()
    }

    @Test
    fun manyClientsTest() {
        val clientList = ArrayList<Chat>()
        val listenersList = ArrayList<ChatEventsListener<ChatUserIpAddr>>()
        val messages = ArrayList<ChatMessage>()
        val n = 10
        (0..n).forEach {
            clientList.add(createClient(it.toString(), if (it != 0) clientList[it - 1].getAddress() else null))
            listenersList.add(mock())
            clientList.last().addChatEventListener(listenersList.last())
            messages.add(createChatMessage(it.toString()))
        }

        clientList.forEach(Chat::startClient)
        Thread.sleep(100)

        clientList.forEachIndexed { i, cFrom ->
            clientList.forEachIndexed { j, cTo ->
                // sending i'th message to j'th client
                cFrom.sendTextMessage(cTo.getMyId(), messages[i])
                Thread.sleep(100)
            }
        }

        Thread.sleep(100)

        listenersList.forEachIndexed { i, lFrom ->
            listenersList.forEachIndexed { j, lTo ->
                if (i == j) {
                    verify(lFrom).messageSent(clientList[j].getMyId(), messages[i])
                } else {
                    verify(lFrom).messageSent(clientList[j].getMyId(), messages[i])
                    verify(lTo).messageReceived(clientList[i].getMyId(), messages[i])
                }
            }
        }


        clientList.forEach(Chat::stopClient)
    }
}
