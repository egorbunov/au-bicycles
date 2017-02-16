package ru.mit.spbau.sd.chat.commons

/**
 * Exception is thrown in case chat net message reader/writer detect any
 * protocol violation like it expects to read CONNECT message, but gets
 * something other
 */
class ProtocolViolation(msg: String): IllegalStateException(msg)