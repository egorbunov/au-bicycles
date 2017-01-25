package ru.spbau.mit.aush.execute.cmd.builtin

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.parse.ArgsPrepare
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream

/**
 * Calculates and writes new line, word and character counts
 * If args string is empty, then data is read from {@code inStream}
 * else every argument is treated as a file and program performs counting
 * for this files
 *
 * Output: lines words chars
 */
class WCExecutor : CmdExecutor() {
    override fun usage(): String =
            "`wc [FILE]...` or `echo hello | wc`"

    val eofString = "EOF"

    override fun name() = "wc"

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        val counts = if (args.isEmpty()) {
            execNoArgs(inStream)
        } else {
            execWithArgs(args)
        }
        val writer = outStream.bufferedWriter()
        writer.write("${counts.first} ${counts.second} ${counts.third}")
        writer.newLine()
        writer.flush()
        return 0
    }

    private fun execNoArgs(input: InputStream): Triple<Int, Int, Int> {
        var lines = 0
        var words = 0
        var chars = 0
        val reader = input.bufferedReader()
        while (true) {
            val line = reader.readLine() ?: break
            if (line == eofString) {
                break
            }
            lines += 1
            words += line.split(Regex("\\s")).size
            chars += line.length + 1
        }

        return Triple(lines, words, chars)
    }

    private fun execWithArgs(args: List<String>): Triple<Int, Int, Int> {
        return args.map { handleOneFile(it) }.fold(Triple(0, 0, 0),
                { a, b -> Triple(a.first + b.first, a.second + b.second, a.third + b.third) })
    }

    private fun handleOneFile(name: String): Triple<Int, Int, Int> {
        var lines = 0
        var words = 0
        var chars = 0
        var prevSpace = true
        val inp = try {
            FileInputStream(name).buffered()
        } catch (e: FileNotFoundException) {
            throw BadCmdArgsError("No such file: $name")
        }
        while (true) {
            val charCode = inp.read()
            if (charCode == -1) {
                break
            }
            chars += 1
            if (Character.isWhitespace(charCode) || Character.isSpaceChar(charCode)) {
                if (!prevSpace) {
                    words += 1
                }
                prevSpace = true
            } else {
                prevSpace = false
            }
            if (charCode == '\n'.toInt()) {
                lines += 1
            }
        }
        return Triple(lines, words, chars)
    }
}
