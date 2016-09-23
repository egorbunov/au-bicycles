package ru.spbau.mit.aush.execute.cmd

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */


/**
 * Every AUSH built in command (such as `echo`)
 * must implement `Cmd` interface
 */
interface Cmd {
    /**
     * @return name of the command, which AUSH user will use to run it
     */
    fun name(): String

    /**
     * Executes command, passing to it given args and
     * with input and output stream specified
     *
     * @param args command line arguments
     * @param inStream input stream
     * @param outStream output stream
     * @return exit code
     */
    fun exec(args: String, inStream: String, outStream: String): Int
}