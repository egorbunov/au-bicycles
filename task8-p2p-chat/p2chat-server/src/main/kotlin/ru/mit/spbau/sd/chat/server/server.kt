package ru.mit.spbau.sd.chat.server

fun main(args: Array<String>) {
    val server = ChatServer(args[0].toInt())

    server.start()
    readLine()
    server.stop()
}
