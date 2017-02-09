package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.SpecialVars
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.log.Logging
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
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

        val newDir = Paths.get(args.first())

        if (isValidDir(newDir)) {
            logger.info("Using absolute path to cd")
            cd(newDir)
        } else {
            val currentDir = Paths.get(System.getenv(SpecialVars.PWD.name))
            val relativeDir = currentDir.resolve(newDir)
            if (isValidDir(relativeDir)) {
                logger.info("Using relative path to cd")
                cd(relativeDir)
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
        val absolutePath = dir.toAbsolutePath().toString()
        AushContext.instance.addVar(SpecialVars.PWD.name, absolutePath)
        logger.info("Changed directory to $absolutePath")
    }
}
