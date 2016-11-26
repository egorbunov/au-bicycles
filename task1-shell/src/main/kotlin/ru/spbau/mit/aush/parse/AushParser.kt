package ru.spbau.mit.aush.parse

import ru.spbau.mit.aush.util.unquote

/**
 *
 */
class AushParser() {
    private val cmdRegex = "(?:\\s*${ParseRegexes.argRegex}\\s*)+"

    fun parse(str: String): Statement? {
        return parseAssign(str) ?: parsePipedCmd(str) ?: parseSimpleCmd(str)
    }

    private fun parseAssign(str: String): Statement.Assign? {
        val pattern = Regex("([\\w_]+)=" +            // {variable name}=
                            "((?:[^\\s'\"]*)" +       // char sequence without spaces
                            "|${ParseRegexes.singleQuotedRegex}" +   // OR double quoted string with escaped chars
                            "|${ParseRegexes.doubleQuotedRegex})")    // OR single quoted string with escaped chars
        val m = pattern.matchEntire(str) ?: return null
        return Statement.Assign(m.groupValues[1], unquote(m.groupValues[2]))
    }

    private fun parsePipedCmd(str: String): Statement.Pipe? {
        val validatePattern = Regex("(?:$cmdRegex\\|)+$cmdRegex")
        validatePattern.matchEntire(str) ?: return null

        val commands = Regex("($cmdRegex)(?:\\|)?")
                .findAll(str)
                .map(MatchResult::groupValues)
                .map { it[1].trim() }
                .map { parseSimpleCmd(it) }
        if (commands.any { it == null }) {
            return null
        }
        return Statement.Pipe(commands.filterNotNull().toList().toTypedArray())
    }

    private fun parseSimpleCmd(str: String): Statement.Cmd? {
        val p = Regex("\\s*(${ParseRegexes.argRegex})((?:\\s*${ParseRegexes.argRegex}\\s*)*)")
        val m = p.matchEntire(str) ?: return null
        return Statement.Cmd(m.groupValues[1].trim(), m.groupValues[2].trim())
    }
}