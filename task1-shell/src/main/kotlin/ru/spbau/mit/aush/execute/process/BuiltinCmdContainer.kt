package ru.spbau.mit.aush.execute.process


import ru.spbau.mit.aush.execute.cmd.CmdExecutor
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.log.Logging
import java.util.*

/**
 * Container main class for builtin AUSH commands;
 * This class is used to run as external process, but for builtin
 * command executor to run inside it.
 */
internal object BuiltinCmdContainer {
    private val logger = Logging.getLogger("BuiltinCmdContainer")

    @JvmStatic fun main(args: Array<String>) {
        val executorClassName = args[0]
        val commandArguments = ArrayList<String>()
        commandArguments.addAll(Arrays.asList(*args).subList(1, args.size))

        val executorClass: Class<*> = try {
            Class.forName(executorClassName)
        } catch (e: ClassNotFoundException) {
            logger.severe("No such executor class found " + executorClassName)
            System.exit(1)
            return
        }

        val cmdExecutor: CmdExecutor = try {
            executorClass.newInstance() as CmdExecutor
        } catch (e: IllegalAccessException) {
            logger.severe("Can't instantiate executor: " + e.message)
            System.exit(1)
            return
        } catch (e: InstantiationException) {
            logger.severe("Can't instantiate executor: " + e.message)
            System.exit(1)
            return
        }

        try {
            val retCode = cmdExecutor.exec(commandArguments, System.`in`, System.out)
            System.exit(retCode)
        } catch (err: BadCmdArgsError) {
            println(err.message)
            println("USAGE: ${cmdExecutor.usage()}")
            System.exit(1)
        } catch (err: CmdExecutionError) {
            println("ERROR: ${err.message}")
            System.exit(1)
        }
    }
}