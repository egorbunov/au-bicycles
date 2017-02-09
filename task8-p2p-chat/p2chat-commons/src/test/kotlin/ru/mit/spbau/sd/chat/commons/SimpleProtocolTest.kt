package ru.mit.spbau.sd.chat.commons

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.createStartReadingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import ru.mit.spbau.sd.chat.commons.net.state.MessageRead


class SimpleProtocolTest {
    @Test
    fun testSimpleProtocolRead() {
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

    @Test
    fun testSimpleProtocolWritingState() {
        val bytes = "hello".toByteArray()
        var state = createStartWritingState(bytes)

        val actualMsgSize = state.getBuffer().getInt()
        state = state.proceed()
        Assert.assertEquals(bytes.size, actualMsgSize)
        bytes.forEach { byte ->
            val actualByte = state.getBuffer().get()
            Assert.assertEquals(byte, actualByte)
            state = state.proceed()
        }
    }
}