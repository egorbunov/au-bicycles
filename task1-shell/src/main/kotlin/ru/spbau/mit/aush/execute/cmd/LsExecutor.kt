package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.log.Logging
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

/**
 * @author Anton Mordberg
 * @since 09.02.17
 */
class LsExecutor  : CmdExecutor() {
    val logger = Logging.getLogger("LsExecutor")
    val separator = "\n"

    override fun name(): String {
        return "ls"
    }

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {
        val target = if (args.isEmpty()) {
            AushContext.instance.getVar(SpecialVars.PWD.name)
        } else if (args.size == 1) {
            args.first()
        } else {
            throw BadCmdArgsError("Too many arguments (you can specify 1 at most)")
        }

        val targetDir = Paths.get(target)
        if (Files.notExists(targetDir)) {
            throw BadCmdArgsError("Specified file or directory was not found")
        }

        val result = ArrayList<String>()

        if (Files.isDirectory(targetDir)) {
            Files.list(targetDir).forEach { x -> result.add(targetDir.relativize(x).toString()) }
        } else {
            result.add(targetDir.toString())
        }

        logger.info("Writing list to buffer...")
        val writer = BufferedWriter(OutputStreamWriter(outStream))
        writer.write(result.joinToString(separator))
        logger.info("Buffer written...flushing...")
        writer.newLine()
        writer.flush()
        return 0
    }
}