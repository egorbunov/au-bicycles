package ru.spbau.mit.aush.parse

import ru.spbau.mit.aush.parse.visitor.StatementVisitor
import java.util.*

/**
 * Base class for every possible shell statement,
 * which is produced by parser and further analyzed
 * and "executed" by executor
 */
sealed class Statement {
    abstract fun accept(visitor: StatementVisitor)

    /**
     * Simple command line utility call statement like:
     *     `echo -n "Hello" "World"`
     */
    class Cmd(val cmdName: String, val args: String) : Statement() {
        override fun accept(visitor: StatementVisitor) {
            visitor.visit(this)
        }

        override fun toString(): String {
            return "CmdExecutor($cmdName${if (args.isEmpty()) "" else ", $args"})"
        }

        override fun equals(other: Any?): Boolean {
            return other is Cmd && other.args == args && other.cmdName == cmdName
        }

        override fun hashCode(): Int {
            var result = cmdName.hashCode()
            result = 31 * result + args.hashCode()
            return result
        }
    }

    /**
     * Commands connected with io redirection form piped statement:
     *     `echo "Hello" | cat`
     */
    class Pipe(val commands: Array<Cmd>) : Statement() {
        override fun accept(visitor: StatementVisitor) {
            visitor.visit(this)
        }

        override fun toString(): String {
            return "Pipes(${commands.foldRight("", { c, s -> if (s.isEmpty()) "$c" else "$c, $s" })})"
        }

        override fun equals(other: Any?): Boolean {
            return other is Pipe && other.commands.size == commands.size &&
                    other.commands.zip(commands).all { it.first == it.second }
        }

        override fun hashCode(): Int {
            return Arrays.hashCode(commands)
        }
    }

    /**
     * That class represents assignment:
     *     `VAR_NAME=VALUE`
     */
    class Assign(val varName: String, val value: String) : Statement() {
        override fun accept(visitor: StatementVisitor) {
            visitor.visit(this)
        }

        override fun toString(): String {
            return "Assign($varName, [$value])"
        }

        override fun equals(other: Any?): Boolean {
            return other is Assign && other.value == value && other.varName == varName
        }

        override fun hashCode(): Int {
            var result = varName.hashCode()
            result = 31 * result + value.hashCode()
            return result
        }
    }
}






