package ru.spbau.mit.aush.execute

import ru.spbau.mit.aush.execute.cmd.AssignCommand
import ru.spbau.mit.aush.execute.cmd.PipedCommand
import ru.spbau.mit.aush.execute.cmd.SimpleCommand
import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.parse.visitor.PrepareVisitor
import ru.spbau.mit.aush.parse.visitor.VarReplacingVisitor

/**
 * Main interpreter class
 * Interpreter has it's own context with environmental variables and exit code.
 * Every command except assign command are executed in separate processes
 *
 * It uses System.in and System.out as in/out command streams, so not to
 * close whole interpreter builtin commands use EOF string as signal to stop
 *
 */
class AushInterpreter(val context: AushContext) {
    init {
        if (context.getVar(SpecialVars.PATH.name) == null) {
            context.addVar(SpecialVars.PATH.name, System.getProperty("user.dir"))
        }
    }

    /**
     * Executes given command as parsed statement
     */
    fun execute(statement: Statement) {
        // replacing variables
        val replacer = VarReplacingVisitor(context)
        val statementWithVars = replacer.replace(statement)

        val preparer = PrepareVisitor()
        val statementToExecute = preparer.prepareStatement(statementWithVars)

        val command = when (statementToExecute) {
            is Statement.Cmd -> SimpleCommand(statementToExecute)
            is Statement.Pipe -> PipedCommand(statementToExecute)
            is Statement.Assign -> AssignCommand(statementToExecute)
        }

        command.eval(context)
    }
}
