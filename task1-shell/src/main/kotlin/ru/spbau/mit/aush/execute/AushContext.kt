package ru.spbau.mit.aush.execute

import java.util.*

/**
 * Class represents state of interpreter, which contains:
 *    - environment variables
 *    - last executed command exit code
 */
class AushContext {
    private val vars = HashMap<String, String>()
    private var lastExitCode: Int = 0

    /**
     * Add new variable binding.
     * If there were variable with given name it's value will be overwritten
     */
    fun addVar(name: String, value: String) {
        vars.put(name, value)
    }

    /**
     * Returns all variable bindings
     */
    fun getVars(): List<Pair<String, String>> {
        return vars.toList()
    }

    /**
     * Returns particular variable binding, in case of no such variable null is returned
     */
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