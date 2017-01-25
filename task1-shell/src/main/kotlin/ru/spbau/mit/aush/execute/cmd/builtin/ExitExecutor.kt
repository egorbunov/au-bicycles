package ru.spbau.mit.aush.execute.cmd.builtin

import java.io.InputStream
import java.io.OutputStream
import kotlin.system.exitProcess

/**
 * Executor of exit command; Just terminates whole process
 */
class ExitExecutor : CmdExecutor() {
    override fun usage(): String = "exit"

    override fun name() = "exit"

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        exitProcess(0)
    }
}
