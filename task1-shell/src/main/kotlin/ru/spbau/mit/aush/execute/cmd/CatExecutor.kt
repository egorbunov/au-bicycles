package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.parse.ArgsPrepare
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.io.*

/**
 * Cat command.
 * Acts almost like standard gnu cat, but if called without
 * arguments it reads input until "EOF" string read
 */
class CatExecutor : CmdExecutor() {
    override fun usage(): String =
        "`cat [FILE]...` or `cmd | cat` or just `cat`"

    private val eofString = "EOF"

    override fun name() = "cat"

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        if (args.isEmpty()) {
            execNoArgs(inStream, outStream)
        } else {
            execWithArgs(args, outStream)
        }
        return 0
    }

    private fun execWithArgs(args: List<String>, output: OutputStream) {
        args.forEach { execForOneFile(it, output) }
    }

    private fun execForOneFile(name: String, output: OutputStream) {
        val reader = try {
            BufferedReader(FileReader(name))
        } catch (e: FileNotFoundException) {
            throw CmdExecutionError("no such file: $name")
        }
        val writer = output.bufferedWriter()
        reader.useLines { lines ->
            lines.forEach { line ->
                writer.write(line)
                writer.newLine()
            }
        }
        writer.flush()
    }

    private fun execNoArgs(input: InputStream, output: OutputStream) {
        val reader = input.bufferedReader()
        val writer = output.bufferedWriter()
        while (true) {
            val line = reader.readLine()
            if (line == null || line == eofString) {
                break
            }
            writer.write(line)
            writer.newLine()
            writer.flush()
        }
        writer.flush()
    }
}

