package ru.spbau.mit.aush.execute.cmd

import java.io.InputStream
import java.io.OutputStream
import kotlin.system.exitProcess

/**
 * Executor of exit command; Just terminates whole process
 */
class ExitExecutor : CmdExecutor() {
    override fun name(): String {
        return "exit"
    }

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        exitProcess(0)
    }
}
