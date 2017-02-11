package ru.mit.spbau.sd.chat.client

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.mockito.Mockito
import ru.mit.spbau.sd.chat.client.model.ChatEventsListener
import ru.mit.spbau.sd.chat.client.model.ChatModel
import ru.mit.spbau.sd.chat.client.net.ChatNetworkShield
import ru.spbau.mit.sd.commons.proto.ChatMessage
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr


open class ChatControllerTest {
    private fun getNetShieldMock(): ChatNetworkShield {
        val netShield: ChatNetworkShield = mock()
        Mockito.doNothing().`when`(netShield).startClient(any())
        Mockito.doNothing().`when`(netShield).stopClient()
        Mockito.doNothing().`when`(netShield).changeClientInfo(any())
        Mockito.doNothing().`when`(netShield).sendChatMessage(any(), any())
        return netShield
    }

    @Test
    fun testNetworkShieldInteraction() {
        val netShield = getNetShieldMock()
        val clientId = ChatUserIpAddr.newBuilder().setIp("2.2.2.2").setPort(33).build()!!
        val clientUserInfo = ChatUserInfo.newBuilder().setName("Michael").build()!!
        val chatModel: ChatModel<ChatUserIpAddr> = ChatModel(clientId, clientUserInfo)
        val chatController = ChatController(netShield, chatModel)

        chatController.startClient()
        verify(netShield).startClient(chatModel.clientInfo)


        val userId = ChatUserIpAddr.newBuilder().setIp("1.1.1.1").setPort(42).build()!!
        val message = ChatMessage.newBuilder().setText("hello").build()!!
        chatModel.addUser(userId, ChatUserInfo.getDefaultInstance())

        chatController.sendTextMessage(userId, message)
        verify(netShield).sendChatMessage(userId, message)

        val newInfo = ChatUserInfo.newBuilder().setName("Mike").build()!!
        chatController.changeClientInfo(newInfo)
        verify(netShield).changeClientInfo(newInfo)

        chatController.stopClient()
        verify(netShield).stopClient()
    }

    @Test
    fun testListenersAreTriggered() {
        val netShield = getNetShieldMock()
        val chatModel: ChatModel<ChatUserIpAddr> =
                ChatModel(createClient("ip", 42), createInfo("Mike"))

        val controller = ChatController(netShield, chatModel)
        val usersEventListener: ChatEventsListener<ChatUserIpAddr> = mock()
        controller.addChatModelChangeListener(usersEventListener)

        // test client started
        val users = listOf(
                Pair(createClient("xxx", 123), createInfo("name1")),
                Pair(createClient("yyy", 231), createInfo("Tom"))
        )
        controller.clientStarted(users)
        for ((id, info) in users) {
            verify(usersEventListener).userBecomeOnline(id, info)
        }

        val userId = createClient("zzz", 1245)
        val userInfo = createInfo("Pahom")

        // test user become online
        controller.userBecomeOnline(userId, userInfo)
        verify(usersEventListener).userBecomeOnline(userId, userInfo)

        val newInfo = createInfo("The Elephant Man")
        // test user changed info
        controller.userChangeInfo(userId, newInfo)
        verify(usersEventListener).userChanged(userId, newInfo)

        // test sent message to user
        val msg = ChatMessage.newBuilder().setText("hello").build()!!
        controller.sendTextMessage(userId, msg)
        verify(usersEventListener).messageSent(userId, msg)

        // test receive message
        controller.userSentMessage(userId, msg)
        verify(usersEventListener).messageReceived(userId, msg)

        // test user gone offline
        controller.userGoneOffline(userId)
        verify(usersEventListener).userGoneOffline(userId)

        // test client stopped
        controller.clientStopped()
        for ((id, info) in users) {
            verify(usersEventListener).userGoneOffline(id)
        }
    }
}
