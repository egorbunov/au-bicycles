package ru.mit.spbau.sd.chat.commons.net

import java.nio.ByteBuffer

/**
 * Represents state, which connection or something else may have.
 * That is writing state meaning that there is writing operation
 * is going on (or it may be finished or not started yet).
 *
 * Writing is supposed to be performed with given java.nio ByteBuffer.
 *
 */
interface WritingState {
    /**
     * Check if any progress was made since last proceed() call.
     *
     * This method returns new state of "writing", it may be the
     * same object or not.
     */
    fun proceed(): WritingState

    /**
     * Returns ByteBuffer, from which bytes will be consumed and
     * wrote somewhere
     */
    fun getBuffer(): ByteBuffer
}
