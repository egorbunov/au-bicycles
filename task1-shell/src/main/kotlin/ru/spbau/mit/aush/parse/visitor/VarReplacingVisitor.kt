package ru.spbau.mit.aush.parse.visitor

import ru.spbau.mit.aush.execute.AushContext
import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.util.isDoubleQuoted
import ru.spbau.mit.aush.util.isNotQuoted
import ru.spbau.mit.aush.util.isSingleQuoted
import java.util.*


class VarReplacingVisitor(val context: AushContext) : StatementVisitor {
    val statementStack = LinkedList<Statement>()

    fun replace(statement: Statement): Statement {
        statement.accept(this)
        assert(statementStack.size == 1)
        return statementStack.last()
    }

    override fun visit(assign: Statement.Assign) {
        val newVarName = replaceVars(assign.varName)
        val newValue = replaceVars(assign.value)
        statementStack.addLast(Statement.Assign(newVarName, newValue))
    }

    override fun visit(cmd: Statement.Cmd) {
        val newCmdName = replaceVars(cmd.cmdName)
        val newArgs= cmd.args.map { if (isSingleQuoted(it)) it else replaceVars(it) }
        statementStack.addLast(Statement.Cmd(newCmdName, newArgs))
    }

    override fun visit(pipe: Statement.Pipe) {
        pipe.commands.forEach { this.visit(it) }
        val newPipeSt = Statement.Pipe(statementStack.map { it as Statement.Cmd }.toTypedArray())
        statementStack.clear()
        statementStack.addLast(newPipeSt)
    }

    private fun replaceVars(str: String): String {
        val replaceNoBrace = context.getVars().fold(str,
                {s, p -> s.replace("$${p.first}", p.second)})
        val replaceInBrace = context.getVars().fold(replaceNoBrace,
                {s, p -> s.replace("\${${p.first}}", p.second)})
        return replaceInBrace
    }
}