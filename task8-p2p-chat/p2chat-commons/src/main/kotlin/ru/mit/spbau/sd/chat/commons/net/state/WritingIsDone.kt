package ru.mit.spbau.sd.chat.commons.net.state

import java.nio.ByteBuffer

/**
 * State, which is used to designate that the writing is done!
 */
class WritingIsDone : WritingState {
    private val buf = ByteBuffer.allocate(0)
    override fun getBuffer(): ByteBuffer {
        return buf
    }

    override fun proceed(): WritingState {
        return this
    }
}
