package ru.spbau.mit.aush.test

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.spbau.mit.aush.execute.cmd.builtin.CatExecutor
import java.io.*
import org.junit.Assert

class CatExecutorTest {
    @Rule
    @JvmField val tmp = TemporaryFolder()

    @Test
    fun testNoArgs() {
        val catExecutor = CatExecutor()
        val linesOutStream = PipedOutputStream()
        val catInput = PipedInputStream(linesOutStream)
        val linesIn = listOf("line1", "line2", "line3", "EOF")
        val writer = BufferedWriter(OutputStreamWriter(linesOutStream))
        linesIn.forEach { writer.write(it); writer.newLine() }
        writer.flush()
        writer.close()

        val catOutput = PipedOutputStream()
        val catOutputIn = PipedInputStream(catOutput)

        catExecutor.exec(emptyList(), catInput, catOutput)
        catInput.close()
        linesOutStream.close()
        writer.close()
        catOutput.close()

        val catOutReader = BufferedReader(InputStreamReader(catOutputIn))
        val linesOut = catOutReader.readLines()

        Assert.assertArrayEquals(linesIn.toTypedArray().sliceArray(0..linesIn.size - 2), linesOut.toTypedArray())
    }

    @Test
    fun testWithArgs() {
        val catExecutor = CatExecutor()
        val filesLines = arrayOf(
                listOf("f1line1", "f1line2", "f1line3"),
                listOf("f2line1", "f2line2", "f2line3"),
                listOf(""),
                listOf("", "", "")
        )
        val fileNames = filesLines.map { createFileWithContents(it) }

        val dummyInputStream = System.`in`
        val catOutput = PipedOutputStream()
        val catOutputIn = PipedInputStream(catOutput)

        catExecutor.exec(fileNames, dummyInputStream, catOutput)
        catOutput.close()

        val catOutReader = BufferedReader(InputStreamReader(catOutputIn))
        val linesOut = catOutReader.readLines()

        val expectedLines = filesLines.flatMap { it }
        Assert.assertArrayEquals(expectedLines.toTypedArray(), linesOut.toTypedArray())
    }

    private fun createFileWithContents(lines: List<String>): String {
        val file = tmp.newFile()
        val writer = BufferedWriter(FileWriter(file))
        lines.forEach {
            writer.write(it)
            writer.newLine()
        }
        writer.flush()
        writer.close()
        return file.absolutePath.toString()
    }
}
