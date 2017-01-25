package ru.spbau.mit.aush.execute.cmd.builtin

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.ArgsPrepare
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter


/**
 * Echo command imitation. Just like linux echo.
 */
class EchoExecutor : CmdExecutor() {
    val logger = Logging.getLogger("EchoExecutor")
    val separator = " "

    override fun name(): String {
        return "echo"
    }

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        logger.info("Writing arguments to buffer...")
        val writer = BufferedWriter(OutputStreamWriter(outStream))
        args.forEachIndexed { i, s ->
            writer.write(s)
            if (i < args.size - 1) {
                writer.write(separator)
            }
        }
        logger.info("Buffer written...flushing...")
        writer.newLine()
        writer.flush()
        return 0
    }
}
