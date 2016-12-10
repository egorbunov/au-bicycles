package ru.spbau.mit.aush.parse.error

/**
 * Error thrown during tokenizer stage. Just another parser error
 */
class TokenErr(msg: String = "") : RuntimeException(msg)