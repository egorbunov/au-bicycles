package ru.spbau.mit.aush.parse

import ru.spbau.mit.aush.util.unquote

object ArgsPrepare {
    fun prepare(tokens: List<String>): List<String> {
        return tokens
                .map(::unquote)
                .map { s ->
                    // escaped characters
                    s.replace(Regex("\\\\(.)"), { mr -> mr.groupValues[1] })
                }.toList()
    }
}
