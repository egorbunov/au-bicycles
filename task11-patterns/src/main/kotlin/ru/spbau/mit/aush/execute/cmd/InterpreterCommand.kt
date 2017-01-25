package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.parse.Statement

/**
 * Alongside builtin commands, which are used for starting processes for aush concrete
 * command interpretation we use Command design pattern for more abstract interpreter
 * constructions: simple commands, piped commands and other commands which, for ex.,
 * evaluate assign statement
 */
interface InterpreterCommand {
    /**
     * Evaluates commnd taking context and returning new one (returning context is not
     * necessary new, it can be the same object...)
     */
    fun eval(ctx: AushContext)
}