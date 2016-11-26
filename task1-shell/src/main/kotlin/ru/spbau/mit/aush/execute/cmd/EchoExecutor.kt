package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.parse.ArgsSplitter
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter


class EchoExecutor : CmdExecutor() {
    val argSplitter = ArgsSplitter()
    val separator = " "

    override fun name(): String {
        return "echo"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {
        val argStrings = try {
            argSplitter.parse(args)
        } catch (e: IllegalArgumentException) {
            throw BadCmdArgsError("Bad echo args =/")
        }

        val writer = BufferedWriter(OutputStreamWriter(outStream))
        argStrings.forEachIndexed { i, s ->
            writer.write(s)
            if (i < argStrings.size - 1) {
                writer.write(separator)
            }
        }
        writer.newLine()
        writer.flush()
        return 0
    }
}
