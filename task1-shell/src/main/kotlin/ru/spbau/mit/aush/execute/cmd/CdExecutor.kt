package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.log.Logging
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

/**
 * CD stands for change directory.
 *
 * Usage: cd FOLDER
 *
 * @author Anton Mordberg
 * @since 09.02.17
 */
class CdExecutor : CmdExecutor() {

    val logger = Logging.getLogger("CdExecutor")

    override fun name(): String {
        return "cd"
    }

    override fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int {

        if (args.size != 1) {
            throw BadCmdArgsError("You must specify a directory")
        }

        val absolutePath = AushContext.instance.getRootDir().resolve(args.first())
        val relativePath = AushContext.instance.getPwd().resolve(args.first())

        if (isValidDir(relativePath)) {
            logger.info("Using relative path to cd")
            cd(relativePath)
        } else {
            if (isValidDir(absolutePath)) {
                logger.info("Using absolute path to cd")
                cd(absolutePath)
            } else {
                logger.info("Failed to change dir")
                throw BadCmdArgsError("No such directory")
            }
        }
        return 0
    }

    private fun isValidDir(dir: Path): Boolean {
        return Files.exists(dir) && Files.isDirectory(dir)
    }

    private fun cd(dir: Path) {
        val absolutePath = dir.toAbsolutePath().normalize().toString()
        AushContext.instance.addVar(SpecialVars.PWD, absolutePath)
        logger.info("Changed directory to $absolutePath")
    }
}
