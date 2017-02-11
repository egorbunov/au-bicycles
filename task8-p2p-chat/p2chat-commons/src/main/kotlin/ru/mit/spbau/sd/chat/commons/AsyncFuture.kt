package ru.mit.spbau.sd.chat.commons

/**
 * Toy future =)
 */
interface AsyncFuture<out T> {
    fun get(): T

    fun <U> thenApply(transform: (T) -> U): AsyncFuture<U> {
        return object: AsyncFuture<U> {
            override fun get(): U {
                return transform(this@AsyncFuture.get())
            }
        }
    }
}
