package ru.spbau.mit.aush.util

import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/24/16
 * Email: egor-mailbox@ya.com
 */


/**
 * Splits given string by given chars with respect
 * to quoted strings, so splits char is in quoted string
 * are not considered
 */
fun splitCmd(str: String, splitChars: Set<Char>): Array<String> {
    val quotes = setOf('\'','"')
    var lastSplit = -1
    var isInQuotedStr = false
    var quoteType: Char? = null
    var prevSym: Char? = null
    var isEscaped: Boolean

    val result = ArrayList<String>()

    for ((i, c) in str.withIndex()) {
        isEscaped = prevSym == '\\'

        if (!isEscaped && c in quotes) {
            if (!isInQuotedStr) {
                isInQuotedStr = true
                quoteType = c
            } else if (c == quoteType) {
                isInQuotedStr = false
                quoteType = null
            }
        }

        if (!isInQuotedStr && c in splitChars) {
            result.add(str.substring(lastSplit+1..i-1))
            lastSplit = i
        }

        prevSym = c
    }
    result.add(str.substring(lastSplit + 1))

    return result.toTypedArray()
}