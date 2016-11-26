package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import ru.spbau.mit.aush.execute.cmd.CatExecutor
import ru.spbau.mit.aush.execute.cmd.WCExecutor
import java.io.*


class WCExecutorTest {
    @Rule
    @JvmField val tmp = TemporaryFolder()

    @Test
    fun testNoArgs() {
        val wcExecutor = WCExecutor()
        val linesOutStream = PipedOutputStream()
        val wcInput = PipedInputStream(linesOutStream)
        val linesIn = listOf("1", "3 3", "4444", "hello world")
        val writer = BufferedWriter(OutputStreamWriter(linesOutStream))
        linesIn.forEach { writer.write(it); writer.newLine() }
        writer.flush()
        writer.close()

        val catOutput = PipedOutputStream()
        val catOutputIn = PipedInputStream(catOutput)

        wcExecutor.exec("", wcInput, catOutput)
        wcInput.close()
        linesOutStream.close()
        writer.close()
        catOutput.close()

        val catOutReader = BufferedReader(InputStreamReader(catOutputIn))
        val linesOut = catOutReader.readLines()
        Assert.assertTrue(linesOut.size == 1)

        val counts = linesIn.fold(Triple(0, 0, 0),
                { t, w -> Triple(
                        t.first + 1,
                        t.second + if (w.isBlank()) 0 else w.trim().split(Regex("\\s")).size,
                        t.third + w.length + 1
                )})

        Assert.assertEquals("${counts.first} ${counts.second} ${counts.third}", linesOut[0])
    }

    @Test
    fun testWithArgs() {
        val wcExecutor = WCExecutor()
        val filesLines = arrayOf(
                listOf("1", "22", "55555"),
                listOf("22", "333", "666666"),
                listOf(""),
                listOf("", "", "")
        )
        val fileNames = filesLines.map { createFileWithContents(it) }

        val argsStr = fileNames.joinToString(" ")

        val wcOutput = PipedOutputStream()
        val wcOutputIn = PipedInputStream(wcOutput)

        wcExecutor.exec(argsStr, System.`in`, wcOutput)
        wcOutput.close()

        val counts = filesLines.flatMap{ it }.fold(Triple(0, 0, 0),
                { t, w -> Triple(
                        t.first + 1,
                        t.second + if (w.isBlank()) 0 else w.trim().split(Regex("\\s")).size,
                        t.third + w.length + 1
                )})


        val wcOutReader = BufferedReader(InputStreamReader(wcOutputIn))
        val linesOut = wcOutReader.readLines()
        Assert.assertTrue(linesOut.size == 1)
        Assert.assertEquals("${counts.first} ${counts.second} ${counts.third}", linesOut[0])
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
