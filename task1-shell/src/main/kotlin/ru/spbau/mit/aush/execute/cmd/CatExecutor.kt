package ru.spbau.mit.aush.execute.cmd

import jdk.internal.util.xml.impl.Input
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.parse.SimpleArgsParser
import java.io.*

/**
 * Cat command.
 * Acts almost like standard gnu cat, but if called without
 * arguments it reads input until 2 consequent new lines are read
 */
class CatExecutor : CmdExecutor() {
    val argSplitter = SimpleArgsParser()

    override fun name(): String {
        return "cat"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {
        val parsedArgs = try {
            argSplitter.parse(args)
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
        var feedCnt = 0
        val reader = input.bufferedReader()
        val writer = output.bufferedWriter()
        while (true) {
            val line = reader.readLine()
            if (line.isEmpty()) {
                feedCnt += 1
            }
            writer.write(line)
            writer.newLine()
            if (feedCnt == 2) {
                break
            }
        }
        writer.flush()
    }
}
