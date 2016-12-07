package ru.spbau.mit.aush.test

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.parse.*
import ru.spbau.mit.aush.parse.visitor.PrepareVisitor
import ru.spbau.mit.aush.parse.visitor.VarReplacingVisitor
import java.util.*


@RunWith(Parameterized::class)
class StatementPrepareTest(val str: String,
                           val expectedStatement: Statement) {
    val parser = AushParser()
    val preparer = PrepareVisitor()

    companion object {

        @Parameterized.Parameters
        @JvmStatic fun testData(): Collection<Any> {
            return Arrays.asList(
                    arrayOf("echo \"double quoted\" 'single quoted' es\\cape '\\'escape'",
                            Statement.Cmd("echo", listOf("double quoted", "single quoted", "escape", "'escape"))),
                    arrayOf("echo '\\e\\s\\c\\a\\p\\e'",
                            Statement.Cmd("echo", listOf("\\e\\s\\c\\a\\p\\e"))),
                    arrayOf("echo 'I\\'m'",
                            Statement.Cmd("echo", listOf("I'm"))),
                    arrayOf("echo \"\\x \\\"\"",
                            Statement.Cmd("echo", listOf("\\x \"")))
            )
        }
    }

    @Test fun testPrepare() {
        val statement = parser.parse(str)
        val actualStatement = preparer.prepareStatement(statement)

        Assert.assertEquals(expectedStatement, actualStatement)
    }
}
