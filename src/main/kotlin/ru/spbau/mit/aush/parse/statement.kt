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
 *
 * Command name (`echo`) is stored in `cmdName`
 *
 * Arguments string (`-n "Hello" "World"`) is stored
 * in `args` property
 */
class CmdStatement(val cmdName: String, val args: String) : Statement() {}

/**
 * Statement with more than one command call connected with
 * pipes (io redirection) parsed to `PipedStatement`:
 *     `echo "Hello" | cat`
 */
class PipedStatement(val cmds: Array<CmdStatement>) : Statement() {}

/**
 * That class represents assignment:
 *     `VARNAME=VALUE`
 */
class AssignStatement(val varName: String, val value: String) : Statement() {}
