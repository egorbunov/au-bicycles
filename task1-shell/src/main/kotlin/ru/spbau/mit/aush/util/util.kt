package ru.spbau.mit.aush.util

fun isNotQuoted(str: String): Boolean {
    return str.length <= 1 || str[0] != str[str.lastIndex] || str[0] !in setOf('\'', '"')
}

fun isSingleQuoted(str: String): Boolean {
    return str.length > 1 && str[0] == str[str.lastIndex] && str[0] == '\''
}

fun isDoubleQuoted(str: String): Boolean {
    return str.length > 1 && str[0] == str[str.lastIndex] && str[0] == '"'
}

/**
 * Deletes only one pair of quotes. Both ' or ", but only in case quote
 * is the same at the start and at the end
 */
fun unquote(str: String): String {
    if (isSingleQuoted(str) or isDoubleQuoted(str)) {
        return str.substring(1, str.lastIndex)
    }
    return str
}
