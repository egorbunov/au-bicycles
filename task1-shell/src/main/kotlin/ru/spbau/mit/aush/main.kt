package ru.spbau.mit.aush

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.AushInterpreter
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.parse.AushParser

/**
 * Created by: Egor Gorbunov
 * Date: 9/21/16
 * Email: egor-mailbox@ya.com
 */


fun main(args: Array<String>) {
    val pareser = AushParser()
    val context = AushContext()
    val interpreter = AushInterpreter(context, System.`in`, System.out)
    val replReader = System.`in`.bufferedReader()
    while (true) {
        print("aush >> ")
        val line = replReader.readLine() ?: break
        if (line.isBlank()) {
            continue
        }
        val statement = pareser.parse(line)
        if (statement === null) {
            println("Error: bad syntax...")
            continue
        }
        try {
            interpreter.execute(statement)
        } catch (e: CmdExecutionError) {
            println("Error: ${e.message}")
        } catch (e: BadCmdArgsError) {
            println("Error: ${e.message}")
        }
    }
}