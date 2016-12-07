package ru.spbau.mit.aush

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.AushInterpreter
import ru.spbau.mit.aush.execute.error.BadCmdArgsError
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.AushParser
import ru.spbau.mit.aush.parse.error.ParseErr
import ru.spbau.mit.aush.parse.error.TokenErr

/**
 * Created by: Egor Gorbunov
 * Date: 9/21/16
 * Email: egor-mailbox@ya.com
 */


fun main(args: Array<String>) {
    val logger = Logging.getLogger("Main")
    val pareser = AushParser()
    val context = AushContext()
    val interpreter = AushInterpreter(context)
    val replReader = System.`in`.bufferedReader()
    while (true) {
        print("aush >> ")
        val line = replReader.readLine() ?: break
        if (line.isBlank()) {
            continue
        }
        logger.info("Parsing statement...")
        val statement = try {
            pareser.parse(line)
        } catch (e: TokenErr) {
            println("${e.message}")
            continue
        } catch (e: ParseErr) {
            println("ERROR: can't parse statement!")
            continue
        }
        try {
            logger.info("Executing statement...")
            interpreter.execute(statement)
        } catch (e: CmdExecutionError) {
            println("${e.message}")
        } catch (e: BadCmdArgsError) {
            println("${e.message}")
        }
    }
}
