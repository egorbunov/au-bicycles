package ru.spbau.mit.aush.execute.cmd

import java.io.InputStream
import java.io.OutputStream

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */
private class PWDExecutor : CmdExecutor() {
    override fun name(): String {
        return "pwd"
    }

    override fun exec(args: String, inStream: InputStream, outStream: OutputStream): Int {
        return 0
    }
}