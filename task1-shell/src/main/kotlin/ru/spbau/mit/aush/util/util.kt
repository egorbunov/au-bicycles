package ru.spbau.mit.aush.util

/**
 * Deletes only one pair of quotes. Both ' or ", but only in case quote
 * is the same at the start and at the end
 */
fun unquote(str: String): String {
    if (str[0] == str[str.lastIndex] && str[0] in setOf('\'', '"')) {
        return str.substring(1, str.lastIndex)
    }
    return str
}
