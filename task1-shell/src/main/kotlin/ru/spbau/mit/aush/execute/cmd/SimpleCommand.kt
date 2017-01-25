package ru.spbau.mit.aush.execute.cmd

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.cmd.builtin.ExitExecutor
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.execute.process.AushCmdProcessBuilderFactory
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.Statement
import java.io.IOException

/**
 * Simple command, that is just one call of one process with some arguments
 */
class SimpleCommand(val statement: Statement.Cmd): InterpreterCommand{
    val logger = Logging.getLogger("PipedCommand")
    val exitCommandName = ExitExecutor().name()

    override fun eval(ctx: AushContext) {
        logger.info("executing ${statement.cmdName}")

        // exiting if exit command in place =)
        if (statement.cmdName == exitCommandName) {
            System.exit(ctx.getExitCode())
        }

        // creating separate process for command execution in all other cases
        val pb = AushCmdProcessBuilderFactory.createPB(statement)
        pb.inheritIO()
        try {
            val process = pb.start()
            logger.info("Waiting for process to finish...")
            ctx.setExitCode(process.waitFor())
        } catch (e: IOException) {
            throw CmdExecutionError("Error running process: ${statement.cmdName}")
        }
    }
}