package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import java.io.InputStream
import java.io.OutputStream

/**
 * user.dir is used as working directory
 */
class PWDExecutor : CmdExecutor() {
    override fun name(): String {
        return "pwd"
    }

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        val writer = outStream.bufferedWriter()
        writer.write(AushContext.instance.getVar(SpecialVars.PWD.name))
        writer.newLine()
        writer.flush()
        return 0
    }
}
