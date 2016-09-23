package ru.spbau.mit.aush.execute.cmd

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */
private class EchoCmd: Cmd {
    override fun name(): String {
        return "echo"
    }

    override fun exec(args: String, inStream: String, outStream: String): Int {
        return 0
    }
}