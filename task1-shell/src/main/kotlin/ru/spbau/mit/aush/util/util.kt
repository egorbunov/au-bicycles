package ru.spbau.mit.aush.util

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */

fun unquote(str: String): String {
    if (str[0] == str[str.lastIndex] && str[0] in setOf('\'', '"')) {
        return str.substring(1, str.lastIndex)
    }
    return str
}