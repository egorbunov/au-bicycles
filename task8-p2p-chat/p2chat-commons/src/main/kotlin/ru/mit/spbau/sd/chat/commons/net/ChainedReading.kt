package ru.mit.spbau.sd.chat.commons.net

import java.nio.ByteBuffer

/**
 * State represents reading in progress with some continuation
 *
 * @param bufToRead buffer, in which data is going to be read
 * @param transition continuation for reading, that is simple function,
 *        which takes result of reading operation from previous chained reading
 *        step and returns, basing on it, new reading state; ByteBuffer is flipped (!)
 *        just before invoking transition continuation so it is prepared for reading
 *        data from it during transition evaluation
 *
 *
 */
class ChainedReading<out T>(private val bufToRead: ByteBuffer,
                            private val transition: (ByteBuffer) -> ReadingState<T>) : ReadingState<T> {
    override fun proceed(): ReadingState<T> {
        if (bufToRead.hasRemaining()) {
            return this
        }

        bufToRead.flip()
        return transition(bufToRead)
    }

    override fun getBuffer(): ByteBuffer {
        return bufToRead
    }

    override fun getMessage(): T {
        throw IllegalStateException("getMessage() at non-terminal state")
    }
}
