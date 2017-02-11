package ru.mit.spbau.sd.chat.client

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.client.model.ChatModel
import ru.spbau.mit.sd.commons.proto.ChatMessage


class ChatModelTest {
    @Test
    fun test() {
        val clientId = createClient("1.1.1.1", 42)
        val clientInfo = createInfo("Mike")
        val model = ChatModel(clientId, clientInfo)

        // test add user
        val userId = createClient("2.2.2.2", 45)
        val userInfo = createInfo("Jacob")
        model.addUser(userId, userInfo)
        Assert.assertEquals(1, model.getUsers().size)
        Assert.assertEquals(Pair(userId, userInfo), model.getUsers()[0])

        // test add message to
        val msg = ChatMessage.newBuilder().setText("Hello").build()!!
        model.addMessageSentByThisUser(userId, msg)
        val box1 = model.getMessages(userId)
        Assert.assertEquals(1, box1.size)
        Assert.assertEquals(Pair(clientId, msg), box1[0])

        // test add message from
        model.addMessageSentByOtherUser(userId, msg)
        val box2 = model.getMessages(userId)
        Assert.assertEquals(2, box2.size)
        Assert.assertEquals(Pair(userId, msg), box2[1])
    }
}