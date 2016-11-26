package ru.spbau.mit.aush.parse


class SimpleArgsParser {
    private val singleQuotedRegex = "'(?:[^'\\\\]|\\\\.)*'"
    private val doubleQuotedRegex = "\"(?:[^\"\\\\]|\\\\.)*\""
    private val argRegex = "(?:$singleQuotedRegex|$doubleQuotedRegex|[^|\"'\\s]+)"

    /**
     * Splits given string into arguments list, so double/single quoted arguments
     * handled correctly
     * @throws IllegalArgumentException in case whole argument string has typos
     *         (badly ordered quotes, actually)
     */
    fun parse(argsStr: String): List<String> {
        if (!Regex("(?:\\s*$argRegex\\s*)*").matches(argsStr)) {
            throw IllegalArgumentException("[$argsStr] does not match argument list regex")
        }
        val args = Regex(argRegex).findAll(argsStr).map(MatchResult::value).toList()
        return args
    }
}