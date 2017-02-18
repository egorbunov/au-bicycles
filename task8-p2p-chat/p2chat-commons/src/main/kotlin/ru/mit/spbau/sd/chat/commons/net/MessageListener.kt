package ru.mit.spbau.sd.chat.commons.net

/**
 * @param T - message, for which listener is listening
 * @param A - attachment, which comes together with the message
 */
interface MessageListener<in T, in A> {

    /**
     * Callback, which is triggered in case new message
     * of type `T` was received.
     *
     * @param attachment - that is additional payload, which is
     * carried together with the message
     */
    fun messageReceived(msg: T, attachment: A)
}
