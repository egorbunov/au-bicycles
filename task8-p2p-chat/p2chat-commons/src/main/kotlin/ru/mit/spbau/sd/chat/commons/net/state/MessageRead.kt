package ru.mit.spbau.sd.chat.commons.net.state

import java.nio.ByteBuffer

/**
 * That is terminal state for reading process -- read message holder
 */
class MessageRead<out T>(private val message: T) : ReadingState<T> {
    private val dummyBuffer = ByteBuffer.allocate(0)
    /**
     * Loops this state
     */
    override fun proceed(): ReadingState<T> {
        return this
    }

    override fun getBuffer(): ByteBuffer {
        return dummyBuffer
    }

    override fun getMessage(): T {
        return message
    }
}
