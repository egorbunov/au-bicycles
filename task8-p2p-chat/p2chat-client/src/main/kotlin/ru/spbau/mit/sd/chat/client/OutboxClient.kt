package ru.spbau.mit.sd.chat.client

/**
 * Part of a client responsible for sending messages to other clients
 */
class OutboxClient(val sender: Void, val reciever: Void) {
    fun sendMessage(message: String) {}
    fun disconnect() {}
}
