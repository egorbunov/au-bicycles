package ru.spbau.mit.aush.execute

import org.apache.commons.io.IOUtils
import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.spbau.mit.aush.execute.cmd.CmdExecutor
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.ArgsSplitter
import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.parse.visitor.VarReplacingVisitor
import java.io.*
import java.util.*

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
    val logger = Logging.getLogger("AushInterpreter")
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
        logger.info("Executing external command: ${statement.cmdName}")

        // prepare arguments
        val argsSplitter = ArgsSplitter()
        val args = try {
            argsSplitter.parse(statement.args)
        } catch (e: IllegalArgumentException) {
            listOf("")
        }
        val command = ArrayList<String>()
        command.add(statement.cmdName)
        if (args.isNotEmpty()) {
            command.addAll(args)
        }

        // run process
        val pb = ProcessBuilder(command)
        pb.directory(File(System.getProperty("user.dir")))
//        pb.inheritIO()
        val process = try {
            pb.start()
        } catch (e: IOException) {
            throw CmdExecutionError("Can't execute command: ${statement.cmdName}")
        }
        while (process.isAlive) {
            process.waitFor()
        }
        IOUtils.copy(process.inputStream, output)
        val ret = process.exitValue()
        context.setExitCode(ret)
        return 0
    }
}


