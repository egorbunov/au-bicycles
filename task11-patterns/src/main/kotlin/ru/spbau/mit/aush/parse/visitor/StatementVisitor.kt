package ru.spbau.mit.aush.parse.visitor

import ru.spbau.mit.aush.parse.Statement

/**
 * Created by: Egor Gorbunov
 * Date: 10/19/16
 * Email: egor-mailbox@ya.com
 */
interface StatementVisitor {
    fun visit(assign: Statement.Assign)
    fun visit(cmd: Statement.Cmd)
    fun visit(pipe: Statement.Pipe)
}