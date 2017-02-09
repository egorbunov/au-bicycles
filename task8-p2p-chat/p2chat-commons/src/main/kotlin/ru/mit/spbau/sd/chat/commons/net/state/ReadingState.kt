package ru.mit.spbau.sd.chat.commons.net.state

import java.nio.ByteBuffer

/**
 * This is logically the same as WritingState, but with
 * reading operation. So instance of ReadingState represents
 * some state of reading operation in progress.
 *
 * Data is read to ByteBuffer
 */
interface ReadingState<out T> {
    fun proceed(): ReadingState<T>
    fun getBuffer(): ByteBuffer
    fun getMessage(): T
}
