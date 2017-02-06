package ru.mit.spbau.sd.chat.commons

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.GatheringWriting
import ru.mit.spbau.sd.chat.commons.net.WritingState
import ru.mit.spbau.sd.chat.commons.net.createStartWritingState
import java.nio.ByteBuffer


class WritingStateTest {
    @Test
    fun testGatheringWriteState() {
        val strs = arrayOf("hello", "write", "state", "test")
        val bsArr = strs.map { it.toByteArray() }

        val buffers = bsArr.map {
            val buf = ByteBuffer.allocate(it.size)
            buf.put(it)
            buf.flip() // prepare for reading from buffer
            buf
        }

        var writeState: WritingState = GatheringWriting(buffers.toTypedArray())

        bsArr.flatMap { it.asIterable() }.forEach { byte ->
            val actual = writeState.getBuffer().get()
            Assert.assertEquals(byte, actual)
            writeState = writeState.proceed()
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
