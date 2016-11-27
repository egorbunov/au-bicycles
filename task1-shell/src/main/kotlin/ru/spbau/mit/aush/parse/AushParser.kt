package ru.spbau.mit.aush.parse

import ru.spbau.mit.aush.util.unquote
import java.util.*

/**
 * Command parser
 */
class AushParser() {

    fun parse(str: String): Statement? {
        val tokenizer = ArgsTokenizer(str)
        val tokens = tokenizer.tokenize()
        return parseAssign(str.trim()) ?: parsePipedCmd(tokens) ?: parseSimpleCmd(tokens)
    }

    private fun parseAssign(str: String): Statement.Assign? {
        val pattern = Regex("([\\w_]+)=" +                            // {variable name}=
                            "((?:[^\\s'\"]*)" +                       // char sequence without spaces
                            "|${ParseRegExes.singleQuotedRegex}" +    // OR double quoted string with escaped chars
                            "|${ParseRegExes.doubleQuotedRegex})")    // OR single quoted string with escaped chars
        val m = pattern.matchEntire(str) ?: return null
        return Statement.Assign(m.groupValues[1], unquote(m.groupValues[2]))
    }

    private fun parsePipedCmd(tokens: List<String>): Statement.Pipe? {
        val pipeIndexes = tokens.mapIndexed { i, s -> if (s == "|") i else -1 }.filter { it != -1 }.toMutableList()
        if (pipeIndexes.isEmpty()) {
            return null
        }
        pipeIndexes.add(tokens.size)
        val commands = ArrayList<Statement.Cmd>()
        var prev = 0
        for (cur in pipeIndexes) {
            val sl = tokens.slice(prev..cur-1)
            val cmd = parseSimpleCmd(sl) ?: return null
            commands.add(cmd)
            prev = cur + 1
        }
        return Statement.Pipe(commands.toTypedArray())
    }

    private fun parseSimpleCmd(tokens: List<String>): Statement.Cmd? {
        if (tokens.isEmpty()) {
            return null
        }
        val args = tokens.slice(1..tokens.size-1)
        return Statement.Cmd(tokens[0], if (args.isEmpty()) "" else args.joinToString(" "))
    }
}