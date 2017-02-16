package ru.mit.spbau.sd.chat.commons

/**
 * Toy future I use instead of standard one, because it is
 * a functional interface =)
 */
interface AsyncFuture<out T> {
    fun get(): T
}
