package ru.spbau.mit.aush

import ru.spbau.mit.aush.parse.AushParser

/**
 * Created by: Egor Gorbunov
 * Date: 9/21/16
 * Email: egor-mailbox@ya.com
 */


fun main(args: Array<String>) {
    val parser = AushParser()
    println(parser.parse("echo \"hello'\" | cat"))
}