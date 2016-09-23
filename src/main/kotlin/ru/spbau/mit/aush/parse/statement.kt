package ru.spbau.mit.aush.parse

/**
 * Created by: Egor Gorbunov
 * Date: 9/23/16
 * Email: egor-mailbox@ya.com
 */

/**
 * Base class for every possible shell statement,
 * which is produced by parser and further analyzed
 * and "executed" by executor
 */
abstract class Statement {}

/**
 * Simple command line utility call statement like:
 *     `echo -n "Hello" "World"`
 */
class CmdStatement(val cmdName: String, val args: String) : Statement() {}

/**
 * Commands connected with io redirection form piped statement:
 *     `echo "Hello" | cat`
 */
class PipedStatement(val cmds: Array<CmdStatement>) : Statement() {}

/**
 * That class represents assignment:
 *     `VAR_NAME=VALUE`
 */
class AssignStatement(val varName: String, val value: String) : Statement() {}
