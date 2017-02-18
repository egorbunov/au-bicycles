package ru.mit.spbau.sd.chat.commons.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.AsyncFuture
import ru.mit.spbau.sd.chat.commons.limit
import ru.mit.spbau.sd.chat.commons.net.state.NothingToWrite
import ru.mit.spbau.sd.chat.commons.net.state.ReadingState
import ru.mit.spbau.sd.chat.commons.net.state.WritingIsDone
import ru.mit.spbau.sd.chat.commons.net.state.WritingState
import java.nio.channels.AsynchronousSocketChannel
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock

/**
 * One asynchronous channel server. This class serves exact one connection, which
 * is done using `AsynchronousSocketChannel`. To setup listening for incoming messages
 * user wants to call `startReading()` method and to destroy channel (passed as constructor
 * argument) and all other server resources user wants to invoke `destroy()`.
 *
 * Also user can use `asyncRead(): Future<T>` before calling `startReading()` only to
 * interact with server in more `synchronous` manner. That is made so for now, but
 * in the Future<=)>...
 *
 * AsyncServer performs asynchronous reading/writing from/to given channel, but
 * to be reusable and generous it need to be wired with a few helpers:
 *
 * @param createReadingState - factory-method, which creates an initial (empty) state,
 *        which provides server with buffer to read data from channel to.
 * @param createWritingState - factory-method, which will be used to create a proper
 *        writing state (with correctly set up buffers for reading from them), so
 *        to write data from it's buffer to channel
 * @param messageListener - the guy, who receives all read messages from socket channel.
 *
 * AsyncServer treats message as fully read in case it's internal reading state,
 * initialized with `createReadingState()` approaches `MessageRead<T>` state.
 *
 * And AsyncServer understood, that message is fully written to socket channel
 * iff created by `createWritingState(msg: U)` writing state approaches `NothingToWrite`
 * state.
 *
 * Every read message from channel will be passed to `messageListener` and for logical
 * reasons it is passed alongside with `AsyncServer` instance (which got this message)
 * itself. That is to be used for answering to message through the same channel and also
 * this thing may be used for proper destruction of `AsyncServer` instance as a reaction
 * on some event
 *
 *
 * @param T - type of message read from channel (request type)
 * @param U - type of message written to channel (response type)
 */
open class AsyncServer<out T, in U, out A>(val channel: AsynchronousSocketChannel,
                                           private val createReadingState: () -> ReadingState<T>,
                                           private val createWritingState: (msg: U) -> WritingState,
                                           private val messageListener: MessageListener<T, AsyncServer<T, U, A>>,
                                           val payload: A? = null,
                                           serverName: String = AsyncServer::class.java.name) {
    val logger = LoggerFactory.getLogger(serverName)!!

    private var readingState = createReadingState()

    private val nothingToWriteState = NothingToWrite()
    /**
     * See notes on atomicity of this field in comments inside `writeMessage` method
     */
    @Volatile
    private var writingState: WritingState = nothingToWriteState

    /**
     * Queue of pending writings.
     * It is possible, that multiple threads will touch this queue,
     * that why all interactions with it are covered with `writingQueueLock`.
     * Hopefully, sections, there this class work with queue, are small, so
     * locking shouldn't be very time-consuming.
     */
    private val writingStatesQueue = LinkedList<WritingState>()

    private val writingQueueLock = ReentrantLock()
    private val writingQueueIsEmpty = writingQueueLock.newCondition()

    @Volatile
    private var isReadingStarted = false


    open fun getHeldPayload(): A? {
        return payload
    }

    /**
     * Starts listening for incoming messages
     */
    open fun startReading() {
        isReadingStarted = true
        initiateAsyncRead()
    }

    /**
     * Starts asynchronous read, recursively (new read initiated after prev. completed)
     */
    private fun initiateAsyncRead() {
        asyncRead(
                channel,
                readingState,
                onComplete = { res: T ->
                    logger.debug("Got message from channel: ${prepareMsg(res)}...")
                    messageListener.messageReceived(res, this@AsyncServer)
                    readingState = createReadingState()
                    initiateAsyncRead()
                },
                onFail = { e ->
                    logger.error("Failed to complete async read: $e")
                }
        )
    }

    private fun <V> prepareMsg(msg: V): String {
        return msg.toString().map { if (Character.isWhitespace(it)) ' ' else it }.joinToString(separator = "").limit(1000)
    }

    /**
     * Initiates one asynchronous read; This method will only work in case
     * `startReading` not called.
     */
    fun asyncRead(): AsyncFuture<T> {
        if (isReadingStarted) {
            throw IllegalStateException("Can't read this way if server is already reading asynchronously " +
                    "(startReading was called)")
        }
        return asyncRead(channel, createReadingState())
    }

    /**
     * Starts asynchronous operation for writing given message to socket channel.
     * This method immediately returns, but if previous writing was not finished yet
     *
     * @param msg message to sent through channel
     * @param onComplete invoked in case of successful write completion
     * @param onFail on fail handler
     */
    open fun writeMessage(msg: U, onComplete: () -> Unit, onFail: (Throwable?) -> Unit) {
        logger.debug("Starting Write: message: ${prepareMsg(msg)}...")

        /*
            I use this synchronization because it seems possible, that
            this method will be used from multiple threads: consider consequent reads
            from channel occur:
                1. first read is completed and completion handler is executed in Thread 1
                2. during Thread 1 work next read is completed and completion handler executed
                   in another thread: Thread 2
            That means it is possible to have two concurrent `writeMessage` calls from 2 threads, because
            `messageListener`, which is invoked by read completion handler, may execute `writeMessage`.
         */

        writingQueueLock.lock()
        try {
            if (writingState == nothingToWriteState) {
                assert(writingStatesQueue.isEmpty())
                // nobody is writing now, so we just setting up new state and starting async. write
                writingState = createWritingState(msg)
                initiateAsyncWrite(onComplete, onFail)
            } else {
                logger.debug("Messages in write queue: ${writingStatesQueue.size}")
                writingStatesQueue.add(createWritingState(msg))
            }
        } finally {
            writingQueueLock.unlock()
        }
    }

    /**
     * Starts new asynchronous write
     */
    private fun initiateAsyncWrite(onComplete: () -> Unit, onFail: (Throwable?) -> Unit) {
        asyncWrite(channel, writingState,
                onComplete = createWriteCompleteHandler(onComplete, onFail),
                onFail = {
                    logger.error("Failed to complete async. write: $it")
                    onFail(it)
                }
        )
    }

    /**
     * Creates proper write complete handler
     */
    private fun createWriteCompleteHandler(onComplete: () -> Unit, onFail: (Throwable?) -> Unit): () -> Unit {
        return {
            logger.debug("Write completed...")
            onComplete()
            // acquiring lock, because we are going to work with the queue
            writingQueueLock.lock()
            try {
                if (writingStatesQueue.isNotEmpty()) {
                    writingState = writingStatesQueue.remove()
                    initiateAsyncWrite(onComplete, onFail)
                } else {
                    // signalling that queue is empty
                    writingQueueIsEmpty.signalAll()
                    writingState = nothingToWriteState
                }
            } finally {
                writingQueueLock.unlock()
            }
        }
    }

    /**
     * write message with empty fail handler and complete handler
     */
    open fun writeMessage(msg: U) {
        writeMessage(msg, {}, {})
    }

    /**
     * Synchronously writing message to channel.
     * This method will wait for all pending writes to complete,
     * and startReading it's own synchronous write to channel.
     *
     * So the write starts when write requests queue becomes empty.
     */
    open fun writeMessageSync(msg: U) {
        writingQueueLock.lock()
        try {
            while (writingStatesQueue.isNotEmpty()) {
                writingQueueIsEmpty.await()
            }
            var wrState = createWritingState(msg)
            while (wrState !is WritingIsDone) {
                try {
                    channel.write(wrState.getBuffer()).get()
                } catch (e: ExecutionException) {
                    throw e.cause!!
                }
                wrState = wrState.proceed()
            }
        } finally {
            writingQueueLock.unlock()
        }
    }

    /**
     * Close channel; cleanup resources.
     * One must call it after work is done.
     */
    open fun destroy() {
        channel.close()
    }
}
