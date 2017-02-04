package ru.mit.spbau.sd.chat.server

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.server.net.ChatModelPeerMsgProcessor
import ru.mit.spbau.sd.chat.server.net.ChatServer

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("main")
    logger.info("Hello =)")
    logger.debug("Debug =)")
    logger.error("Error =)")
    val chatModel = ConcurrentChatModel()
    val peerEventProcessor = ChatModelPeerMsgProcessor(chatModel)
    val chatServer = ChatServer(peerEventProcessor)

    chatServer.setup()

    readLine()

    chatServer.destroy()
}
