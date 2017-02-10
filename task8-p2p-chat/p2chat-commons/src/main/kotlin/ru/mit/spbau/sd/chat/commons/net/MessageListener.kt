package ru.mit.spbau.sd.chat.commons.net

/**
 * @param T - message, for which listener is listening
 * @param A - attachment, which comes together with the message
 */
interface MessageListener<in T, in A> {
    fun messageReceived(msg: T, attachment: A)
}

/**
 * Listener, which does noting
 */
class UselessMessageListener<in T, in A>: MessageListener<T, A> {
    override fun messageReceived(msg: T, attachment: A) {
    }
}
