package ru.spbau.mit.aush.execute

import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * Class represents state of interpreter, which contains:
 *    - environment variables
 *    - last executed command exit code
 */
class AushContext private constructor() {
    private val vars = HashMap<String, String>()
    private var lastExitCode: Int = 0

    init {
        val workDir = if (System.getenv().containsKey(SpecialVars.PWD.name)) {
            System.getenv(SpecialVars.PWD.name)
        } else {
            System.getProperty("user.dir")
        }
        addVar(SpecialVars.PATH.name, workDir)
        addVar(SpecialVars.PWD.name, workDir)
    }

    private object instanceHolder { val instance = AushContext() }

    companion object {
        val instance: AushContext by lazy { instanceHolder.instance }
    }

    fun getPwd(): Path = Paths.get(getVar(SpecialVars.PWD))

    fun getRootDir(): Path {
        var root = getPwd()
        while (root.parent != null) {
            root = root.parent
        }
        return root
    }

    /**
     * Add new variable binding.
     * If there were variable with given name it's value will be overwritten
     */
    fun addVar(name: String, value: String) {
        vars.put(name, value)
        System.setProperty(name, value)
    }

    /**
     * Add new variable binding.
     * If there were variable with given name it's value will be overwritten
     */
    fun addVar(specialVar: SpecialVars, value: String) {
        addVar(specialVar.name, value)
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

    /**
     * Returns particular variable binding, in case of no such variable null is returned
     */
    fun getVar(specialVar: SpecialVars): String? {
        return vars[specialVar.name]
    }

    fun setExitCode(code: Int) {
        lastExitCode = code
    }

    fun getExitCode(): Int {
        return lastExitCode
    }
}