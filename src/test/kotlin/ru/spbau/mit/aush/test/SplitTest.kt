package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.aush.util.splitCmd
import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */

@RunWith(Parameterized::class)
class SplitTest(val str: String,
                val splitChars: Set<Char>,
                val expectedSplit: Array<String>) {
    companion object {

        @Parameterized.Parameters
        @JvmStatic fun testData(): Collection<Any> {
            return Arrays.asList(
                    arrayOf("echo HELLO | cat",
                            setOf('|'),
                            arrayOf("echo HELLO ", " cat")),
                    arrayOf("echo '|' | cat",
                            setOf('|'),
                            arrayOf("echo '|' ", " cat")),
                    arrayOf("\"a | b | c | d\"",
                            setOf('|'),
                            arrayOf("\"a | b | c | d\"")),
                    arrayOf("echo \"Hello \\\" | \" | cat", // str: "Hello \" | "
                            setOf('|'),
                            arrayOf("echo \"Hello \\\" | \" ", " cat")),
                    arrayOf("' | \\\" | ' | a",
                            setOf('|'),
                            arrayOf("' | \\\" | ' ", " a")),
                    arrayOf("echo 'hello world' -a -b -c",
                            setOf(' '),
                            arrayOf("echo", "'hello world'", "-a", "-b", "-c")),
                    arrayOf("echo 'hello \\'world\\'' -a -b -c",
                            setOf(' '),
                            arrayOf("echo", "'hello \\'world\\''", "-a", "-b", "-c"))
            )
        }
    }

    @Test fun testStrSplit() {
        val actualSplit = splitCmd(str, splitChars)
        Assert.assertArrayEquals(expectedSplit, actualSplit)
    }

}