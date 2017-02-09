package ru.mit.spbau.sd.chat.commons.net

interface MessageListener<in T, in A> {
    fun messageReceived(msg: T, attachment: A)
}
