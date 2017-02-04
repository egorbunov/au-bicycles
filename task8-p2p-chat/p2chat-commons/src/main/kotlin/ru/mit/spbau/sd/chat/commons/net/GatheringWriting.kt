package ru.mit.spbau.sd.chat.commons.net

import java.nio.ByteBuffer

/**
 * This writing state represents progress of writing many byte buffers somewhere.
 *
 */
class GatheringWriting(private val buffers: Array<ByteBuffer>) : WritingState {
    var currentBufIdx = 0

    init {
        if (buffers.isEmpty()) {
            throw IllegalArgumentException("buffers array must be at least of size 1")
        }
    }

    override fun proceed(): WritingState {
        val curBuf = buffers[currentBufIdx]
        if (!curBuf.hasRemaining()) {
            if (currentBufIdx == buffers.size - 1) {
                return NothingToWrite()
            } else {
                currentBufIdx += 1
            }
        }
        return this
    }

    override fun getBuffer(): ByteBuffer {
        return buffers[currentBufIdx]
    }
}
