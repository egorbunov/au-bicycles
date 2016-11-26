package ru.spbau.mit.aush.parse

import java.util.regex.Matcher


class ArgsSplitter {
    /**
     * Splits given string into arguments list, so double/single quoted arguments
     * handled correctly
     * @throws IllegalArgumentException in case whole argument string has typos
     *         (badly ordered quotes, actually)
     */
    fun parse(argsStr: String): List<String> {
        if (!Regex("(?:\\s*${ParseRegexes.argRegex}\\s*)*").matches(argsStr)) {
            throw IllegalArgumentException("[$argsStr] does not match argument list regex")
        }
        val args = Regex(ParseRegexes.argRegex)
                .findAll(argsStr)
                .map(MatchResult::value)
                .map { s ->
                    if (s.first() == s.last() && (s.first() == '\'' || s.first() == '"')) {
                        s.substring(1, s.lastIndex)
                    } else {
                        s
                    }
                }
                .map { s->
                    s.replace(Regex("\\\\(.)"), { mr -> mr.groupValues[1] })
                }
                .toList()
        return args
    }
}