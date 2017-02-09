package ru.mit.spbau.sd.chat.commons.net

import ru.mit.spbau.sd.chat.commons.intSizeInBytes
import ru.mit.spbau.sd.chat.commons.net.*
import ru.mit.spbau.sd.chat.commons.net.state.*
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
    val bufToBeWritten = ByteBuffer.allocate(messageToWrite.size + intSizeInBytes())
    bufToBeWritten.putInt(messageToWrite.size)
    bufToBeWritten.put(messageToWrite)
    bufToBeWritten.flip()
    return GatheringWriting(arrayOf(bufToBeWritten))
}
