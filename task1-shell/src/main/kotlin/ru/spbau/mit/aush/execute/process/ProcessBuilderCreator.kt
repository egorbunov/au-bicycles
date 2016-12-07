package ru.spbau.mit.aush.execute.process

import java.io.File
import java.util.*


object ProcessBuilderCreator {
    /**
     * Creates process builder for built in command; Such commands are executed with help of JVM;
     * @param cmdExecutorClass class of command executor to be prepared as a process
     * @param arguments arguments of command executor
     */
    fun createBuiltinCmdPB(cmdExecutorClass: String, arguments: List<String>): ProcessBuilder {
        val jvm = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
        val classpath = System.getProperty("java.class.path")
        val command = ArrayList<String>()
        command.add(jvm)
        command.add(BuiltinCmdContainer::class.java.canonicalName)
        command.add(cmdExecutorClass)
        for (arg in arguments) {
            command.add(arg)
        }
        val processBuilder = ProcessBuilder(command)
        val environment = processBuilder.environment()
        environment.put("CLASSPATH", classpath)
        return processBuilder
    }

    /**
     * Creates simple process builder for not builtin command. That's just
     * process with command name and its arguments
     */
    fun createExternalCmdPB(cmd: String, args: List<String>): ProcessBuilder {
        val command = ArrayList<String>()
        command.add(cmd)
        if (args.isNotEmpty()) {
            command.addAll(args)
        }
        val pb = ProcessBuilder(command)
        return pb
    }
}