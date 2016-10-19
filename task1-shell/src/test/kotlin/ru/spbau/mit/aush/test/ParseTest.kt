package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.aush.parse.*
import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */

@RunWith(Parameterized::class)
class ParseTest(val str: String,
                val expectedStatement: Statement) {
    val parser = AushParser()

    companion object {

        @Parameterized.Parameters
        @JvmStatic fun testData(): Collection<Any> {
            return Arrays.asList(
                    arrayOf("echo HELLO | cat",
                            PipedStatement(arrayOf(
                                    CmdStatement("echo", "HELLO"),
                                    CmdStatement("cat", "")))),
                    arrayOf("VAR_NAME='value with spaces'",
                            AssignStatement("VAR_NAME", "value with spaces")),
                    arrayOf("cmd a \"b\" 'c' '|' \"|\" '\"\\''",
                            CmdStatement("cmd", "a \"b\" 'c' '|' \"|\" '\"\\''"))
            )
        }
    }

    @Test fun testParse() {
        val actualParse = parser.parse(str)
        Assert.assertTrue(actualParse == expectedStatement)
    }

}