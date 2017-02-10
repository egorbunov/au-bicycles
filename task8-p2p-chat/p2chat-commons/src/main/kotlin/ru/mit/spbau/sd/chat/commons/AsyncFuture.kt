package ru.mit.spbau.sd.chat.commons


interface AsyncFuture<out T> {
    fun get(): T
}