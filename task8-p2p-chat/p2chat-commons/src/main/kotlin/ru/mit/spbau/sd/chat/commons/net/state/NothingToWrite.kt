package ru.mit.spbau.sd.chat.commons.net.state

import java.nio.ByteBuffer

/**
 * State represents, that there is no data to
 * write. That is somewhat dummy writing state.
 */
class NothingToWrite : WritingState {
    private val buf = ByteBuffer.allocate(0)
    override fun getBuffer(): ByteBuffer {
        return buf
    }

    override fun proceed(): WritingState {
        return this
    }
}
