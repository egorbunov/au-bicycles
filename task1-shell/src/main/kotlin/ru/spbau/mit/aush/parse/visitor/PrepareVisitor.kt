package ru.spbau.mit.aush.parse.visitor

import ru.spbau.mit.aush.parse.Statement
import ru.spbau.mit.aush.util.isDoubleQuoted
import ru.spbau.mit.aush.util.isNotQuoted
import ru.spbau.mit.aush.util.isSingleQuoted
import ru.spbau.mit.aush.util.unquote
import java.util.*

/**
 * Creates new statement, there unquoting is performed and escaped characters handled:
 *
 * 1) if one token is quoted --> perform all escaped characters unpacking (delete slash)
 * 2) it token is not quoted --> perform de-escaping quotes inside
 */
class PrepareVisitor : StatementVisitor {
    val statementStack = LinkedList<Statement>()

    fun prepareStatement(statement: Statement): Statement {
        statement.accept(this)
        assert(statementStack.size == 1)
        return statementStack.last()
    }

    private fun prepare(str: String): String {
        if (isSingleQuoted(str)) {
            return unquote(str).replace(Regex("\\\\'"), "'")
        } else if (isDoubleQuoted(str)) {
            return unquote(str).replace(Regex("\\\\\""), "\"")
        } else {
            return str.replace(Regex("\\\\(.)"), { it.groupValues[1] })
        }
    }

    override fun visit(assign: Statement.Assign) {
        val newValue = prepare(assign.value)
        statementStack.addLast(Statement.Assign(assign.varName, newValue))
    }

    override fun visit(cmd: Statement.Cmd) {
        val newCmdName = prepare(cmd.cmdName)
        val newArgs= cmd.args.map { prepare(it) }
        statementStack.addLast(Statement.Cmd(newCmdName, newArgs))
    }

    override fun visit(pipe: Statement.Pipe) {
        pipe.commands.forEach { this.visit(it) }
        val newPipeSt = Statement.Pipe(statementStack.map { it as Statement.Cmd }.toTypedArray())
        statementStack.clear()
        statementStack.addLast(newPipeSt)
    }
}