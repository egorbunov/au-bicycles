package ru.spbau.mit.aush.execute

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.spbau.mit.aush.execute.cmd.CmdExecutor
import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.parse.visitor.StatementVisitor
import ru.spbau.mit.aush.parse.visitor.VarReplacingVisitor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.logging.Logger

/**
 * Main interpreter class
 * It must be initialized with context and in/out streams to read/write
 * from.
 * Commands are read from `baseIn` and print their output to `baseOut`
 * During instantiation interpreter adds special variables to `context`
 * only if they are not already present in this context.
 * Special variables are enumerated in `SpecialVars` class
 */
class AushInterpreter(val context: AushContext,
                      val baseIn: InputStream,
                      val baseOut: OutputStream) {
    val logger = Logger.getLogger("AushInterpreter")
    val executors: Map<String, CmdExecutor>

    init {
        if (context.getVar(SpecialVars.PATH.name) == null) {
            context.addVar(SpecialVars.PATH.name, System.getProperty("user.dir"))
        }

        val cmdPackage = "ru.spbau.mit.aush"

        // auto wiring AUSH built in commands executors
        val r = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner())
                .setUrls(ClasspathHelper.forPackage(cmdPackage)))
        executors = r.getSubTypesOf(CmdExecutor::class.java)
                .map { it.newInstance() }.map { it.name() to it }.toMap()
    }

    /**
     * Executes given command as parsed statement
     */
    fun execute(statement: Statement) {
        // replacing variables
        val replacer = VarReplacingVisitor(context)
        val statementWithVars = replacer.replace(statement)

        when (statementWithVars) {
            is Statement.Cmd -> execSimpleCmd(statementWithVars)
            is Statement.Pipe -> execPipedCmd(statementWithVars)
            is Statement.Assign -> execAssignCmd(statementWithVars)
        }
    }

    private fun execSimpleCmd(statement: Statement.Cmd,
                              input: InputStream = baseIn,
                              output: OutputStream = baseOut) {
        logger.info("executing ${statement.cmdName}")

        val exitCode = if (executors[statement.cmdName] != null) {
            executors[statement.cmdName]!!.exec(statement.args, input, output)
        } else {
            executeExternalCmd(statement, input, output)
        }
        context.setExitCode(exitCode)
    }

    private fun execPipedCmd(statement: Statement.Pipe) {
        var input: InputStream = baseIn
        var output: ByteArrayOutputStream = ByteArrayOutputStream()

        for ((i, cmd) in statement.commands.withIndex()) {
            if (i == statement.commands.lastIndex) {
                execSimpleCmd(cmd, input, baseOut)
            } else {
                execSimpleCmd(cmd, input, output)
            }
            input = ByteArrayInputStream(output.toByteArray())
            output = ByteArrayOutputStream()
        }
    }

    private fun execAssignCmd(statement: Statement.Assign) {
        logger.info("Adding var: ${statement.varName}, value = ${statement.value}")
        context.addVar(statement.varName, statement.value)
        context.setExitCode(0)
    }

    private fun executeExternalCmd(statement: Statement.Cmd,
                                   input: InputStream,
                                   output: OutputStream): Int {
        return 0
    }
}

// TODO: implement Commands executors
// TODO: add test for interpreter with mocked input and output streams
// TODO: add tests for every executor
// TODO: implement external command executor

