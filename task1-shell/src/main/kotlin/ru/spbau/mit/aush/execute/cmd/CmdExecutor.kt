package ru.spbau.mit.aush.execute.cmd

import java.io.InputStream
import java.io.OutputStream


/**
 * Every AUSH built in command (such as `echo`) executor
 * must be inherited from `CmdExecutor`
 *
 * WARN: Every executor inherited from `CmdExecutor` *must* have default constructor!
 */
abstract class CmdExecutor {
    /**
     * @return name of the command, which is used by AUSH user to run this command
     */
    abstract fun name(): String

    /**
     * Executes command, passing to it given args and
     * with input and output stream specified
     *
     * @param args command line arguments
     * @param inStream input stream
     * @param outStream output stream
     * @return exit code
     */
    abstract fun exec(args: List<String>, inStream: InputStream, outStream: OutputStream): Int
}
