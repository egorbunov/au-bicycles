package ru.mit.spbau.sd.chat.commons.net

import ru.mit.spbau.sd.chat.commons.AsyncFuture
import ru.mit.spbau.sd.chat.commons.net.state.MessageRead
import ru.mit.spbau.sd.chat.commons.net.state.ReadingState
import ru.mit.spbau.sd.chat.commons.net.state.WritingIsDone
import ru.mit.spbau.sd.chat.commons.net.state.WritingState
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.concurrent.CountDownLatch


/**
 * Asynchronous read from channel
 *
 * @param channel channel to read from
 * @param initReadingState initial reading state, which buffer will be used to read to
 * @param onComplete completion handler
 * @param onFail failure handler
 */
fun <T> asyncRead(channel: AsynchronousSocketChannel,
                  initReadingState: ReadingState<T>,
                  onComplete: (T) -> Unit,
                  onFail: (Throwable?) -> Unit) {
    var readingState = initReadingState
    channel.read(readingState.getBuffer(), null, object : CompletionHandler<Int, Nothing?> {
        override fun completed(result: Int?, attachment: Nothing?) {
            readingState = readingState.proceed()
            if (readingState is MessageRead<T>) {
                onComplete(readingState.getMessage())
            } else {
                if (channel.isOpen) {
                    channel.read(readingState.getBuffer(), null, this)
                }
            }
        }

        override fun failed(exc: Throwable?, attachment: Nothing?) {
            onFail(exc)
        }
    })
}

/**
 * The same as asyncRead with completion handler, but returns Future
 */
fun <T> asyncRead(channel: AsynchronousSocketChannel, initReadingState: ReadingState<T>): AsyncFuture<T> {
    val countdown = CountDownLatch(1)
    // I'am not sure if these should be thread safely accessed or not
    var result: T? = null
    var exception: Throwable? = null
    var fail = false

    asyncRead(channel, initReadingState,
            onComplete = { res: T ->
                result = res
                countdown.countDown()
            },
            onFail = { e ->
                fail = true
                exception = e
                countdown.countDown()
            }
    )

    return object : AsyncFuture<T> {
        override fun get(): T {
            countdown.await()
            if (!fail) {
                return result!!
            } else {
                throw Exception(exception)
            }
        }
    }
}

/**
 * Initiates asynchronous writing to channel. Writing is performed until
 * writing state becomes `WritingIsDone`.
 *
 * @param initWritingState initial writing state
 */
fun asyncWrite(channel: AsynchronousSocketChannel,
               initWritingState: WritingState,
               onComplete: () -> Unit,
               onFail: (Throwable?) -> Unit) {
    var writingState = initWritingState
    channel.write(
            writingState.getBuffer(),
            null,
            object : CompletionHandler<Int, Nothing?> {
                override fun failed(exc: Throwable?, attachment: Nothing?) {
                    onFail(exc)
                }

                override fun completed(result: Int?, attachment: Nothing?) {
                    writingState = writingState.proceed()
                    if (writingState is WritingIsDone) {
                        onComplete()
                    } else {
                        channel.write(writingState.getBuffer(), null, this)
                    }
                }
            }
    )
}

/**
 * Same as asyncWrite with handlers, but returning future instead
 */
fun asyncWrite(channel: AsynchronousSocketChannel, initWritingState: WritingState): AsyncFuture<Unit> {
    val countdown = CountDownLatch(1)
    // I'am not sure if these should be thread safely accessed or not
    // hope that countdown saving everything from breaking
    var exception: Throwable? = null
    var fail = false

    asyncWrite(channel, initWritingState,
            onComplete = {
                countdown.countDown()
            },
            onFail = {
                fail = true
                exception = it
                countdown.countDown()
            }
    )

    return object : AsyncFuture<Unit> {
        override fun get() {
            countdown.await()
            if (fail) {
                throw Exception(exception)
            }
        }

    }
}
