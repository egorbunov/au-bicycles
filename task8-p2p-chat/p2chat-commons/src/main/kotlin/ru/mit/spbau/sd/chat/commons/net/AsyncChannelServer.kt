package ru.mit.spbau.sd.chat.commons.net

import org.slf4j.LoggerFactory
import ru.mit.spbau.sd.chat.commons.net.state.*
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import java.util.*
import java.util.concurrent.atomic.AtomicReference

/**
 * One asynchronous channel server. This class serves exact one connection, which
 * is done using `AsynchronousSocketChannel`. To setup listening for incoming messages
 * user wants to call `start()` method and to destroy channel (passed as constructor
 * argument) and all other server resources user wants to invoke `destroy()`.
 *
 * AsyncChannelServer performs asynchronous reading/writing from/to given channel, but
 * to be reusable and generous it need to be wired with a few helpers:
 *
 * @param createReadingState - factory-method, which creates an initial (empty) state,
 *        which provides server with buffer to read data from channel to.
 * @param createWritingState - factory-method, which will be used to create a proper
 *        writing state (with correctly set up buffers for reading from them), so
 *        to write data from it's buffer to channel
 * @param messageListener - the guy, who receives all read messages from socket channel.
 *
 * AsyncChannelServer treats message as fully read in case it's internal reading state,
 * initialized with `createReadingState()` approaches `MessageRead<T>` state.
 *
 * And AsyncChannelServer understood, that message is fully written to socket channel
 * iff created by `createWritingState(msg: U)` writing state approaches `NothingToWrite`
 * state.
 *
 * Every read message from channel will be passed to `messageListener` and for logical
 * reasons it is passed alongside with `AsyncChannelServer` instance (which got this message)
 * itself. That is to be used for answering to message through the same channel and also
 * this thing may be used for proper destruction of `AsyncChannelServer` instance as a reaction
 * on some event
 *
 *
 * @param T - type of message read from channel (request type)
 * @param U - type of message written to channel (response type)
 */
class AsyncChannelServer<T, in U>(private val channel: AsynchronousSocketChannel,
                                  private val createReadingState: () -> ReadingState<T>,
                                  private val createWritingState: (msg: U) -> WritingState,
                                  private val messageListener: MessageListener<T, AsyncChannelServer<T, U>>) {

    companion object {
        val logger = LoggerFactory.getLogger(AsyncChannelServer::class.java)!!
    }



    private var readingState = createReadingState()

    private val nothingToWriteState = NothingToWrite()
    /**
     * See notes on atomicity of this field in comments inside `writeMessage` method
     */
    private val writingState: AtomicReference<WritingState> = AtomicReference(nothingToWriteState)

    /**
     * Queue of pending writings. It is blocking for the same reasons
     * as `writingState` is volatile (see comment above)
     */
    private val writingStatesQueue = LinkedList<WritingState>()



    /**
     * Starts listening for incoming messages
     */
    fun start() {
        channel.read(readingState.getBuffer(), null, object : CompletionHandler<Int, Nothing?> {
            override fun completed(result: Int?, attachment: Nothing?) {
                logger.debug("Completing async read...")

                readingState = readingState.proceed()
                if (readingState is MessageRead<T>) {
                    val message = readingState.getMessage()
                    logger.debug("Got message from channel: $message")
                    messageListener.messageReceived(message, this@AsyncChannelServer)
                    // renewing reading state, so server is again available
                    // for new incoming messages
                    readingState = createReadingState()
                }
                // subscribing again (in both cases of read and not fully read message)
                channel.read(readingState.getBuffer(), null, this)
            }

            override fun failed(exc: Throwable?, attachment: Nothing?) {
                logger.error("Failed to complete async read: $exc")
            }
        })
    }

    /**
     * Starts asynchronous operation for writing given message to socket channel.
     * This method immediately returns, but if previous writing was not finished yet
     *
     * @param msg message to sent through channel
     * @param onComplete invoked in case of successful write completion
     * @param onFail on fail handler
     */
    fun writeMessage(msg: U, onComplete: () -> Unit, onFail: (Throwable?) -> Unit) {
        logger.debug("Adding message write request, message: $msg")

        /*
            Ugly synchronization. I use it because it seems possible, that
            this method will be used from multiple threads: consider consequent reads
            from channel occur:
                1. first read is completed and completion handler is executed in Thread 1
                2. during Thread 1 work next read is completed and completion handler executed
                   in another thread: Thread 2
            That means it is possible to have two concurrent `writeMessage` calls from 2 threads, because
            `messageListener`, which is invoked by read completion handler, may execute `writeMessage`.
         */
        synchronized(writingStatesQueue) {
            if (writingState.get() == nothingToWriteState) {
                assert(writingStatesQueue.isEmpty())

                // nobody is writing now, so we just setting up new state and starting async. write

                writingState.set(createWritingState(msg))
                channel.write(writingState.get().getBuffer(), null, object : CompletionHandler<Int, Nothing?> {
                    override fun completed(result: Int?, attachment: Nothing?) {
                        logger.debug("Completing async write...")

                        // This write to `writingState` is not covered by synchronized section,
                        // so no happens-before rules apply in case writingState is not volatile or something,
                        // that's why it is atomic (TODO: just volatile is fine?)
                        writingState.set(writingState.get().proceed())

                        if (writingState.get() is WritingIsDone) {
                            logger.debug("Whole message written to channel!")
                            onComplete()
                            synchronized(writingStatesQueue) {
                                if (writingStatesQueue.isNotEmpty()) {
                                    writingState.set(writingStatesQueue.remove())
                                    channel.write(writingState.get().getBuffer(), null, this)
                                } else {
                                    writingState.set(nothingToWriteState)
                                }
                            }
                        } else {
                            channel.write(writingState.get().getBuffer(), null, this)
                        }
                    }

                    override fun failed(exc: Throwable?, attachment: Nothing?) {
                        logger.error("Failed to complete async. write: $exc")
                        onFail(exc)
                    }
                })
            } else {
                logger.debug("Messages in write queue: ${writingStatesQueue.size}")
                writingStatesQueue.add(createWritingState(msg))
            }
        }
    }

    /**
     * write message with empty fail handler and complete handler
     */
    fun writeMessage(msg: U) {
        writeMessage(msg, {}, {})
    }

    /**
     * Close channel; cleanup resources.
     * One must call it after work is done.
     */
    fun destroy() {
        channel.close()
    }
}
