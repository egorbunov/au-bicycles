package ru.spbau.mit.aush.execute.process

import org.apache.commons.io.IOUtils
import ru.spbau.mit.aush.log.Logging


/**
 * Runnable, which pipes output of one process to input of another
 */
class ProcessPiper(val processFrom: Process,val processTo: Process) : Runnable {
    val logger = Logging.getLogger("ProcessPiper")

    override fun run() {
        try {
            IOUtils.copy(processFrom.inputStream, processTo.outputStream)
        } catch (e: Exception) {
            logger.severe("Error piping: ${e.message}")
        } finally {
            processFrom.inputStream.close()
            processTo.outputStream.close()
        }
    }
}