package ru.spbau.mit.aush.execute

import java.util.*

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */

class AushContext {
    private val vars = HashMap<String, String>()

    fun addVar(name: String, value: String) {
        vars.put(name, value)
    }

    fun getVars(): List<Pair<String, String>> {
        return vars.toList()
    }
}