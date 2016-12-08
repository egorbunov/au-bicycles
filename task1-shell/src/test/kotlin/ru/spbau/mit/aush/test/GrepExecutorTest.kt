package ru.spbau.mit.aush.test

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import ru.spbau.mit.aush.execute.cmd.GrepExecutor
import java.io.*

class GrepExecutorTest {
    var grepInput: PipedInputStream? = null
    var grepOutput: PipedOutputStream? = null
    var linesOut: List<String> = emptyList()

    val lines = listOf(
            "the word is mine",
            "noise",
            "WoRd",
            "sword",
            "42 word"
    )

    @Before
    fun setup() {
        val linesOutStream = PipedOutputStream()
        grepInput = PipedInputStream(linesOutStream)

        val writer = BufferedWriter(OutputStreamWriter(linesOutStream))
        lines.forEach { writer.write(it); writer.newLine() }
        writer.flush()
        writer.close()

        grepOutput = PipedOutputStream()
    }

    fun runGrep(flags: List<String>) {
        val grepOutReader = PipedInputStream(grepOutput).bufferedReader()
        val grepExecutor = GrepExecutor()
        grepExecutor.exec(flags, grepInput!!, grepOutput!!)
        grepInput!!.close()
        grepOutput!!.close()
        linesOut = grepOutReader.readLines()
    }

    @Test
    fun simpleTest() {
        runGrep(listOf("word"))
        Assert.assertEquals(listOf("the word is mine", "sword", "42 word"),
                linesOut)
    }

    @Test
    fun testCaseInsensitive() {
        runGrep(listOf("-i", "word"))
        Assert.assertEquals(
                listOf("the word is mine", "WoRd", "sword", "42 word"),
                linesOut
        )
    }

    @Test
    fun testMatchWord() {
        runGrep(listOf("-w", "word"))
        Assert.assertEquals(
                listOf("the word is mine", "42 word"),
                linesOut
        )
    }

    @Test
    fun testAfterMatchLines() {
        runGrep(listOf("-A", "1", "word"))
        Assert.assertEquals(
                listOf("the word is mine", "noise", "sword", "42 word"),
                linesOut
        )
    }

    @Test
    fun testAtStart() {
        runGrep(listOf("^word"))
        Assert.assertEquals(
                emptyList<String>(),
                linesOut
        )
    }

    @Test
    fun testAtEnd() {
        runGrep(listOf("word$"))
        Assert.assertEquals(
                listOf("sword", "42 word"),
                linesOut
        )
    }
}