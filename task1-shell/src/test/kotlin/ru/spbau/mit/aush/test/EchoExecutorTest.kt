package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import ru.spbau.mit.aush.execute.cmd.EchoExecutor
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream


class EchoExecutorTest {

    @Test
    fun simpleTest() {
        val echoExecutor = EchoExecutor()
        val out = PipedOutputStream()
        val outIn = PipedInputStream(out)

        val args = listOf("a", "b", "\"a and b\"", "'a and b'", "\\n", "'\\''", "\"\\\"\"")
        val expectedStr = "a b a and b a and b n ' \""
        val argsStr = args.joinToString(" ")


        echoExecutor.exec(argsStr, System.`in`, out)
        out.close()

        val reader = BufferedReader(InputStreamReader(outIn))

        val lines = reader.readLines()
        Assert.assertTrue(lines.size == 1)
        val line = lines[0]

        Assert.assertEquals(expectedStr, line)
    }
}
