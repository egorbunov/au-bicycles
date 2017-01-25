package ru.spbau.mit.aush.execute.cmd

import org.apache.commons.io.IOUtils
import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.execute.error.CmdExecutionError
import ru.spbau.mit.aush.execute.process.AushCmdProcessBuilderFactory
import ru.spbau.mit.aush.execute.process.ProcessPiper
import ru.spbau.mit.aush.log.Logging
import ru.spbau.mit.aush.parse.Statement
import java.io.IOException

/**
 * Evaluation of command like 'x | y | z'
 */
class PipedCommand(val statement: Statement.Pipe): InterpreterCommand{
    val logger = Logging.getLogger("PipedCommand")

    override fun eval(ctx: AushContext) {
        logger.info("Executing piped command: $statement")
        val pbs = statement.commands.map { AushCmdProcessBuilderFactory.createPB(it) }
        val processes = pbs.map {
            try {
                it.start()
            } catch (e: IOException) {
                logger.severe("Error starting process: ${it.command()}")
                throw CmdExecutionError("Can't start process in pipe: ${it.command()}")
            }
        }
        val piperThreads = processes.zip(processes.slice(1..processes.size-1))
                .map { Thread(ProcessPiper(it.first, it.second)) }
        piperThreads.forEach(Thread::start)
        piperThreads.forEach(Thread::join)
        val lastProcess = processes.last()
        try {
            IOUtils.copy(lastProcess.inputStream, System.out)
        } catch (e: IOException) {
            logger.severe("Error copying last output in pipe command: ${e.message}")
            throw CmdExecutionError("Error executing pipe")
        } finally {
            lastProcess.inputStream.close()
        }
        // calculating exit code (and waiting for process termination)
        ctx.setExitCode(if (processes.map { it.waitFor() }.all { it == 0 }) 0 else 1)
    }
}