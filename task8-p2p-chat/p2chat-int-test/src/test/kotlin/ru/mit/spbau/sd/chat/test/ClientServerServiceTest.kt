package ru.mit.spbau.sd.chat.test

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.mir.spbau.sd.chat.client.net.ChatServerService
import ru.mit.spbau.sd.chat.commons.userIpToSockAddr
import ru.mit.spbau.sd.chat.server.ChatServer
import ru.mit.spbau.sd.chat.server.net.UserMapPeerMsgListener
import ru.spbau.mit.sd.commons.proto.ChatUserInfo
import ru.spbau.mit.sd.commons.proto.ChatUserIpAddr
import java.net.InetSocketAddress
import java.util.*


class ClientServerServiceTest {
    val serverUsersMap = Collections.synchronizedMap(HashMap<InetSocketAddress, ChatUserInfo>())!!
    val chatServer = ChatServer(0, UserMapPeerMsgListener(serverUsersMap))
    val clientId = ChatUserIpAddr.newBuilder().setIp("1.1.1.1").setPort(42).build()!!
    val clientInfo = ChatUserInfo.newBuilder().setName("Michael").build()!!
    var serverService = null as ChatServerService?

    @Before
    fun before() {
        chatServer.start()
        serverService = ChatServerService(
                chatServer.address(),
                clientId,
                clientInfo
        )
    }

    @After
    fun after() {
        chatServer.stop()
    }

    @Test
    fun testStartChatting() {
        val users = serverService!!.startChatting().get()
        Assert.assertEquals(listOf(Pair(clientId, clientInfo)), users)
        Assert.assertEquals(1, serverUsersMap.size)

        val users1 = serverService!!.getUsers().get()
        Assert.assertEquals(listOf(Pair(clientId, clientInfo)), users1)

    }

    @Test
    fun testStopChatting() {
        serverService!!.startChatting().get()
        serverService!!.stopChatting()
        Thread.sleep(250)
        Assert.assertEquals(0, serverUsersMap.size)
    }

    @Test
    fun testChangeInfo() {
        val newInfo = ChatUserInfo.newBuilder().setName("Walter").build()
        serverService!!.startChatting().get()
        serverService!!.changeClientInfo(newInfo)
        Thread.sleep(250)
        Assert.assertEquals(1, serverUsersMap.size)
        Assert.assertEquals(newInfo, serverUsersMap[userIpToSockAddr(clientId)])
    }
}