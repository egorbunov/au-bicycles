package ru.spbau.mit.aush.execute

import java.util.*

class AushContext {
    private val vars = HashMap<String, String>()
    private var lastExitCode: Int = 0

    fun addVar(name: String, value: String) {
        vars.put(name, value)
    }

    fun getVars(): List<Pair<String, String>> {
        return vars.toList()
    }

    fun getVar(name: String): String? {
        return vars[name]
    }

    fun setExitCode(code: Int) {
        lastExitCode = code
    }

    fun getExitCode(): Int {
        return lastExitCode
    }
}