package ru.spbau.mit.aush.execute.cmd.builtin

import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.log.Logging
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/**
 * Grep command imitation.
 * USAGE:
 *     grep [keys] <pattern> <file>
 *     echo hello | grep [keys] <pattern>
 * Supported keys:
 *     . -i   -- case insensitive matching
 *     . -w   -- only whole word matching
 *     . -A n -- print n lines after match
 *
 * If reading from stdin to end grep-'session' user
 * must input 'EOF'
 */
class GrepExecutor : CmdExecutor() {
    private val logger = Logging.getLogger("GrepExecutor")
    private val eofString = "EOF"

    @Option(name = "-i", usage = "make matching case insensitive")
    private var caseInsensitive: Boolean = false

    @Option(name = "-w", usage = "match only whole words")
    private var wholeWords: Boolean = false

    @Option(name = "-A", usage = "number of lines after match to print")
    private var numLinesAfterMatch: Int = 0

    @Argument
    private var otherArgs = ArrayList<String>()

    override fun usage(): String =
            "`grep [flags] <regex> <file>` or `echo smth | grep [flags] <regex>`"

    override fun name() = "grep"

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        val cliParser = CmdLineParser(this)

        try {
            cliParser.parseArgument(args)
        } catch (e: CmdLineException) {
            throw BadCmdArgsError("${e.message}")
        }

        val regex = otherArgs.getOrElse(0, { throw BadCmdArgsError("No regex (pattern) specified") })
        val file = otherArgs.getOrElse(1, { "" })

        val finalRegex = if (wholeWords) {
            "\\b$regex\\b"
        } else {
            regex
        }

        if (file.isEmpty()) {
            grepStream(finalRegex, inStream, outStream)
        } else {
            val fStream = try {
                FileInputStream(file)
            } catch (e: FileNotFoundException) {
                throw CmdExecutionError("File [$file] not found")
            }
            grepStream(regex, fStream, outStream)
        }

        return 0
    }

    private fun grepStream(regexStr: String, input: InputStream, output: OutputStream) {
        val flags = if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0
        val pattern = try {
            Pattern.compile(regexStr, flags)
        } catch (e: PatternSyntaxException) {
            throw CmdExecutionError("${e.description} at ${e.index} in ${e.pattern}")
        }

        val reader = input.bufferedReader()
        val writer = output.bufferedWriter()

        var toPrint = 0

        while (true) {
            val line = reader.readLine()
            if (line == null || line == eofString) {
                break
            }

            val m = pattern.matcher(line)
            if (m.find()) {
                toPrint = numLinesAfterMatch + 1
            }

            if (toPrint > 0) {
                writer.write(line)
                writer.newLine()
                writer.flush()

                toPrint -= 1
            }
        }
        writer.flush()
    }
}
