package ru.spbau.mit.aush.parse

import ru.spbau.mit.aush.parse.error.TokenErr
import java.util.*


class ArgsTokenizer(val str: String) {
    val separatorTokens = setOf('|')

    /**
     * Splits given string into arguments list, so double/single quoted arguments
     * handled correctly
     * @throws IllegalArgumentException in case whole argument string has typos
     *         (badly ordered quotes, actually)
     */
    fun tokenize(): List<String> {
        val tokens = ArrayList<String>()
        var i = -1
        var sep = false
        while (i < str.lastIndex) {
            i += 1
            if (Character.isSpaceChar(str[i])) {
                sep = true
                continue
            } else if (str[i] in separatorTokens) {
                sep = true
                tokens.add(str[i].toString())
                continue
            }
            val p = if (str[i] == '\'') {
                consumeRegex(ParseRegExes.singleQuotedRegex, i)
            } else if (str[i] == '\"') {
                consumeRegex(ParseRegExes.doubleQuotedRegex, i)
            } else {
                consumeRegex(ParseRegExes.wordWithEscapedChars, i)
            }
            if (!sep && tokens.isNotEmpty()) {
                val newToken = tokens.removeAt(tokens.lastIndex) + p.second
                tokens.add(newToken)
            } else {
                tokens.add(p.second)
            }
            sep = false
            i = p.first
        }
        return tokens
    }

    private fun consumeRegex(r: Regex, i: Int): Pair<Int, String> {
        val m = r.find(str, i)
        if (m == null || m.range.first != i) {
            throw TokenErr("Error on substring: ${str.substring(i)}")
        }
        return Pair(m.range.last, m.value)
    }
}