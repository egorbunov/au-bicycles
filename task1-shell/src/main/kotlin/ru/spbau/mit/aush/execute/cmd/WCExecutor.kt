package ru.spbau.mit.aush.execute.cmd

import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream

/**
 * Calculates and writes new line, word and character counts
 * If args string is empty, then data is read from {@code inStream}
 * else every argument is treated as a file and program performs counting
 * for this files
 */
class WCExecutor : CmdExecutor() {
    override fun name(): String {
        return "wc"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {

        return 0
    }

    private fun executeNoArgs(input: InputStream): Triple<Int, Int, Int> {
        val reader = input.bufferedReader()

        return Triple(0, 0, 0)
    }

    private fun executeWithArgs(args: List<String>): Triple<Int, Int, Int> {
        return Triple(0, 0, 0)
    }

    private fun handleOneFile(name: String): Triple<Int, Int, Int> {
        return Triple(0, 0, 0)
    }
}