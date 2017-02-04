package ru.mit.spbau.sd.chat.commons

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.ChainedReading
import ru.mit.spbau.sd.chat.commons.net.MessageRead
import ru.mit.spbau.sd.chat.commons.net.ReadingState
import java.nio.ByteBuffer


class ReadingStateTest {
    @Test
    fun testChainedReading() {
        val buf1 = ByteBuffer.allocate(intSizeInBytes())
        var readingState: ReadingState<String> = ChainedReading(buf1) { buf ->
            val size = Math.max(intSizeInBytes(), buf.getInt())
            val buf2 = ByteBuffer.allocate(size)
            ChainedReading(buf2) { buf ->
                val strLen = Math.abs(buf.getInt(0))
                MessageRead("x".repeat(strLen))
            }
        }

        val targetStr = "xxxxxxxxxxxx"
        readingState.getBuffer().putInt(intSizeInBytes() * 4)
        readingState = readingState.proceed()
        for (i in 0..3) {
            readingState.getBuffer().putInt(targetStr.length)
        }
        readingState = readingState.proceed()
        Assert.assertTrue(readingState is MessageRead)
        with(readingState as MessageRead<String>) {
            Assert.assertEquals(targetStr, readingState.getMessage())
        }
    }

    @Test
    fun simpleProtocolTest() {
        var readingState = createStartReadingState { bytes ->
            String(bytes)
        }

        val strToWrite = "alphabet"
        val bytes = strToWrite.toByteArray()

        readingState.getBuffer().putInt(bytes.size)
        readingState = readingState.proceed()
        readingState.getBuffer().put(bytes)
        readingState = readingState.proceed()

        with(readingState as MessageRead<String>) {
            Assert.assertEquals(strToWrite, readingState.getMessage())
        }
    }
}
