package ru.spbau.mit.aush.parse


object ArgsPrepare {
    fun prepare(tokens: List<String>): List<String> {
        return tokens
                .map { s ->
                    if (s.first() == s.last() && (s.first() == '\'' || s.first() == '"')) {
                        s.substring(1, s.lastIndex)
                    } else {
                        s
                    }
                }
                .map { s ->
                    s.replace(Regex("\\\\(.)"), { mr -> mr.groupValues[1] })
                }.toList()
    }
}
