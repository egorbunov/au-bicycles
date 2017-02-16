package ru.spbau.mit.aush.execute.cmd.builtin

import java.io.InputStream
import java.io.OutputStream

/**
 * user.dir is used as working directory
 */
class PWDExecutor : CmdExecutor() {
    override fun usage(): String =
        "pwd"

    override fun name() = "pwd"

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        val writer = outStream.bufferedWriter()
        writer.write(System.getProperty("user.dir"))
        writer.newLine()
        writer.flush()
        return 0
    }
}
