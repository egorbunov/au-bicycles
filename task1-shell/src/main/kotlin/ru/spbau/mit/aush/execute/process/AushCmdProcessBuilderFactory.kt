package ru.spbau.mit.aush.execute.process

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import ru.spbau.mit.aush.execute.cmd.builtin.CmdExecutor
import ru.spbau.mit.aush.parse.Statement
import java.io.File
import java.util.*

/**
 * Factory class, which creates ProcessBuilder's for command process
 * creation
 */
object AushCmdProcessBuilderFactory {
    private val executorsClassNames: Map<String, String>

    init {
        val cmdPackage = "ru.spbau.mit.aush.execute.cmd.builtin"
        // auto wiring AUSH built in commands executors
        val r = Reflections(ConfigurationBuilder()
                .setScanners(SubTypesScanner())
                .setUrls(ClasspathHelper.forPackage(cmdPackage)))
        executorsClassNames = r.getSubTypesOf(CmdExecutor::class.java)
                .map { Pair(it.newInstance(), it) }
                .map { it.first.name() to it.second.canonicalName }
                .toMap()
    }

    fun createPB(cmd: Statement.Cmd): ProcessBuilder {
        val pb = if (cmd.cmdName in executorsClassNames) {
            createBuiltinCmdPB(
                    executorsClassNames[cmd.cmdName]!!,
                    cmd.args
            )
        } else {
            createExternalCmdPB(cmd.cmdName, cmd.args)
        }
        return pb
    }

    /**
     * Creates process builder for built in command; Such commands are executed with help of JVM;
     * @param cmdExecutorClass class of command executor to be prepared as a process
     * @param arguments arguments of command executor
     */
    private fun createBuiltinCmdPB(cmdExecutorClass: String, arguments: List<String>): ProcessBuilder {
        val jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val classpath = System.getProperty("java.class.path")
        val command = ArrayList<String>()
        command.add(jvm)
        command.add(BuiltinCmdContainer::class.java.canonicalName)
        command.add(cmdExecutorClass)
        command += arguments
        val processBuilder = ProcessBuilder(command)
        val environment = processBuilder.environment()
        environment.put("CLASSPATH", classpath)
        return processBuilder
    }

    /**
     * Creates simple process builder for not builtin command. That's just
     * process with command name and its arguments
     */
    private fun createExternalCmdPB(cmd: String, args: List<String>): ProcessBuilder {
        val command = ArrayList<String>()
        command.add(cmd)
        if (args.isNotEmpty()) {
            command.addAll(args)
        }
        val pb = ProcessBuilder(command)
        return pb
    }
}