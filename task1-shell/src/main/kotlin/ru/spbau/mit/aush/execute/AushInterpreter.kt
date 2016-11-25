package ru.spbau.mit.aush.execute

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.spbau.mit.aush.execute.cmd.CmdExecutor
import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.parse.visitor.StatementVisitor
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

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

    private fun replaceVars(str: String) {
        context.getVars().forEach {
            str.replace("$${it.first}", it.second)
            str.replace("$\\{${it.first}\\}", it.second)
        }
    }

    /**
     * Executes given command as parsed statement
     */
    fun execute(statement: Statement) {
        // replacing variables
        statement.accept(object : StatementVisitor {
            override fun visit(assign: Statement.Assign) {
                replaceVars(assign.varName)
                replaceVars(assign.value)
            }

            override fun visit(cmd: Statement.Cmd) {
                replaceVars(cmd.cmdName)
                replaceVars(cmd.args)
            }

            override fun visit(pipe: Statement.Pipe) {
                pipe.commands.forEach { this.visit(it) }
            }
        })

        when (statement) {
            is Statement.Cmd -> execSimpleCmd(statement)
            is Statement.Pipe -> execPipedCmd(statement)
            is Statement.Assign -> execAssignCmd(statement)
        }
    }

    private fun execSimpleCmd(statement: Statement.Cmd,
                              input: InputStream = baseIn,
                              output: OutputStream = baseOut) {
        println("executing ${statement.cmdName}")

        if (executors[statement.cmdName] != null) {
            executors[statement.cmdName]!!.exec(statement.args, input, output)
            return
        }

        // executing command as external one
        println("EXTERNAL =(")
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
        context.addVar(statement.varName, statement.value)
        context.setExitCode(0)
    }
}