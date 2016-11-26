package ru.spbau.mit.aush.parse


object ParseRegexes {
    val singleQuotedRegex = "'(?:[^'\\\\]|\\\\.)*'"
    val doubleQuotedRegex = "\"(?:[^\"\\\\]|\\\\.)*\""
    val wordWithEscapedQuotes = "(?:(?:\\\\.)|(?:[^\\\\|\"'\\s]))+"
    val argRegex = "(?:$singleQuotedRegex|$doubleQuotedRegex|$wordWithEscapedQuotes)"


    val test = Regex("(?:'(?:[^'\\\\]|\\\\.)*'|\"(?:[^\"\\\\]|\\\\.)*\"|(?:(?:\\\\.)|(?:[^\\\\|\"'\\s]))+)")
}