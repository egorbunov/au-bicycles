package ru.spbau.mit.aush.log

import java.io.File
import java.util.logging.FileHandler
import java.util.logging.Logger
import java.util.logging.SimpleFormatter


object Logging {
    private val fileHandler: FileHandler

    init {
        val tempFile = File.createTempFile("aush_log", ".txt")
        val logPath = tempFile.toPath().toString()
        fileHandler = FileHandler(logPath)
        val formatter = SimpleFormatter()
        fileHandler.formatter = formatter
    }

    @JvmStatic fun getLogger(name: String): Logger {
        val logger = Logger.getLogger(name)
        logger.useParentHandlers = false
        logger.addHandler(fileHandler)
        return logger
    }
}