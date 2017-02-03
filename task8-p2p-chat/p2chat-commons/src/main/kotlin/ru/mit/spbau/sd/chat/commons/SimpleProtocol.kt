package ru.mit.spbau.sd.chat.commons

import ru.mit.spbau.sd.chat.commons.net.*
import java.nio.ByteBuffer

/**
 * Creates simple initial reading state for protocol, where
 * all messages are passed just after their size (4 bytes)
 *
 * @param messageParser function, which parses message from bytes
 */
fun <T> createStartReadingState(messageParser: (ByteArray) -> T): ReadingState<T> {
    val sizeBuffer = ByteBuffer.allocate(intSizeInBytes())
    // first reading message size
    return ChainedReading(sizeBuffer) { buf ->
        val msgSize = buf.getInt(buf.position())
        val msgByteBuffer = ByteBuffer.allocate(msgSize)
        // reading message itself after
        ChainedReading(msgByteBuffer) { msgBuf ->
            // parsing message and returning terminal state
            val resultMessageBytes = ByteArray(msgBuf.remaining())
            msgBuf.get(resultMessageBytes)
            MessageRead(messageParser(resultMessageBytes))
        }
    }
}

/**
 * Creates simple writing state, which buffer is just combined of message size prefix
 * and message bytes itself
 */
fun createStartWritingState(messageToWrite: ByteArray): WritingState {
    val bufferToWrite = ByteBuffer.allocate(messageToWrite.size + intSizeInBytes())
    bufferToWrite.putInt(messageToWrite.size)
    bufferToWrite.put(bufferToWrite)
    return GatheringWriting(arrayOf(bufferToWrite))
}