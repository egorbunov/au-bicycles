package ru.spbau.mit.aush.parse


object ParseRegExes {
    val singleQuotedRegex = Regex("'(?:[^'\\\\]|\\\\.)*'")
    val doubleQuotedRegex = Regex("\"(?:[^\"\\\\]|\\\\.)*\"")
    val wordWithEscapedChars = Regex("(?:(?:\\\\.)|(?:[^\\\\|\"'\\s]))+")
}