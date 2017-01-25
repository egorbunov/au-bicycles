package ru.spbau.mit.aush.execute.error

/**
 * Exception, which should be thrown in case of any error during
 * commands execution
 */
class CmdExecutionError(msg: String) : RuntimeException(msg)
