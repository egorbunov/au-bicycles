package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.parse.*
import ru.spbau.mit.aush.parse.visitor.VarReplacingVisitor
import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */

@RunWith(Parameterized::class)
class VarReplaceTest(val str: String,
                     val expectedStatement: Statement) {
    val parser = AushParser()
    val context = AushContext()
    val replacer = VarReplacingVisitor(context)

    init {
        context.addVar("X", "hello")
        context.addVar("y", "world")
        context.addVar("CMD", "echo")
    }

    companion object {

        @Parameterized.Parameters
        @JvmStatic fun testData(): Collection<Any> {
            return Arrays.asList(
                    arrayOf("echo \$X",
                            Statement.Cmd("echo", "hello")),
                    arrayOf("echo \${y}_end",
                            Statement.Cmd("echo", "world_end")),
                    arrayOf("echo \$X\$y",
                            Statement.Cmd("echo", "helloworld")),
                    arrayOf("\$CMD \${X} \${y}",
                            Statement.Cmd("echo", "hello world"))
            )
        }
    }

    @Test fun testReplace() {
        val statement = parser.parse(str)!!
        val actualReplaceStatement = replacer.replace(statement)
        Assert.assertTrue(actualReplaceStatement == expectedStatement)
    }

}