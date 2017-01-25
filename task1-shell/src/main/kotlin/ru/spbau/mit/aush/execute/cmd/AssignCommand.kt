package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.Statement

/**
 * Assignment evaluation: evaluation of "x=y".
 */
class AssignCommand(val statement: Statement.Assign): InterpreterCommand{
    val logger = Logging.getLogger("AssignCommand")

    override fun eval(ctx: AushContext) {
        logger.info("Adding var: ${statement.varName}, value = ${statement.value}")
        ctx.addVar(statement.varName, statement.value)
        ctx.setExitCode(0)
    }
}