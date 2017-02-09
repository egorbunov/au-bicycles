package ru.mit.spbau.sd.chat.commons

import org.junit.Assert
import org.junit.Test
import ru.mit.spbau.sd.chat.commons.net.state.GatheringWriting
import ru.mit.spbau.sd.chat.commons.net.state.WritingState
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
}
