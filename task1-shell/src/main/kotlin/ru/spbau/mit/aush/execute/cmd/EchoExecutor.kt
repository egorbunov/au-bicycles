package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.ArgsPrepare
import ru.spbau.mit.aush.parse.ArgsTokenizer
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter


class EchoExecutor : CmdExecutor() {
    val logger = Logging.getLogger("EchoExecutor")
    val separator = " "

    override fun name(): String {
        return "echo"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {
        logger.info("Parsing arguments...")
        val argStrings = try {
            ArgsPrepare.prepare(ArgsTokenizer(args).tokenize())
        } catch (e: IllegalArgumentException) {
            throw BadCmdArgsError("Bad echo args =/")
        }
        logger.info("Writing them to buffer...")
        val writer = BufferedWriter(OutputStreamWriter(outStream))
        argStrings.forEachIndexed { i, s ->
            writer.write(s)
            if (i < argStrings.size - 1) {
                writer.write(separator)
            }
        }
        logger.info("Buffer written...flushing...")
        writer.newLine()
        writer.flush()
        return 0
    }
}
