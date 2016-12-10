package ru.spbau.mit.aush.parse.error

/**
 * Error, which is thrown in case statement cannot be parsed
 */
class ParseErr(msg: String = "") : RuntimeException(msg)
