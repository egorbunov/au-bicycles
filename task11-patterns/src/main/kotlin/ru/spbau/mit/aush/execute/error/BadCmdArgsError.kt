package ru.spbau.mit.aush.execute.error

/**
 * Exception should be thrown in case of error during parsing cmd arguments
 */
class BadCmdArgsError(msg: String) : RuntimeException(msg)
