package ru.mit.spbau.sd.chat.commons.net

import java.nio.ByteBuffer

/**
 * Represents state, which connection or something else may have.
 * Writing state means that there is writing operation
 * is going on (or it may be finished or not started yet).
 *
 * Writing is supposed to be performed with given java.nio ByteBuffer.
 *
 * Suppose we want to write N bytes to socket; Using this WritingState
 * approach you have to:
 *     1. Put these N bytes into ByteBuffer and prepare this buffer
 *        to be sent through socket (so you want to flip the buffer
 *        after putting bytes in it)
 *     2. Initialize WritingState with that ready-to-be-written buffer
 *     3. loop until buffer has no remaining items (it's position
 *        hit it's limit, meaning that everything from the buffer
 *        was written somewhere) doing:
 *             tryWriteToSocket(state.getBuffer())
 *             state = state.proceed()
 *
 * See how that is implemented using `NotingToWrite` state and `GatheringWriting` state.
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
     * wrote somewhere.
     *
     * These method supposes, that returned ByteBuffer may be changed
     * from outside, because the other only method of these class `proceed`
     * is here to check if something in the buffer have changed and to
     * decide how to change the state accordingly to changes in ByteBuffer
     */
    fun getBuffer(): ByteBuffer
}
