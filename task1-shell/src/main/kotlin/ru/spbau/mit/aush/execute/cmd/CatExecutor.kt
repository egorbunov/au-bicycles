package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.io.*

/**
 * Cat command.
 * Acts almost like standard gnu cat, but if called without
 * arguments it reads input until "EOF" string read
 */
class CatExecutor : CmdExecutor() {
    val eofString = "EOF"

    override fun name(): String {
        return "cat"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {
        val parsedArgs = try {
            ArgsTokenizer(args).tokenize()
        } catch (e: IllegalArgumentException) {
            throw BadCmdArgsError("Bad cat args =/")
        }
        if (parsedArgs.isEmpty()) {
            execNoArgs(inStream, outStream)
        } else {
            execWithArgs(parsedArgs, outStream)
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
