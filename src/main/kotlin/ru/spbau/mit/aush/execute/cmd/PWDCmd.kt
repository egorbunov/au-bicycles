package ru.spbau.mit.aush.execute.cmd

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */
private class PWDCmd: Cmd {
    override fun name(): String {
        return "pwd"
    }

    override fun exec(args: String, inStream: String, outStream: String): Int {
        return 0
    }
}