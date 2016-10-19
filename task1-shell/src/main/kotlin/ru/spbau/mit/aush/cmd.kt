package ru.spbau.mit.aush

import java.io.OutputStream

/**
 * Created by: Egor Gorbunov
 * Date: 9/21/16
 * Email: egor-mailbox@ya.com
 */


interface ICmd {
    fun name(): String
    fun exec(args: String, inStream: String, outStream: String): Int
}

private class CmdEcho : ICmd {
    override fun name(): String {
        return "echo"
    }

    override fun exec(args: String, inStream: String, outStream: String): Int {
        println("args = $args; in = $inStream; out = $outStream")
        return 0
    }
}

