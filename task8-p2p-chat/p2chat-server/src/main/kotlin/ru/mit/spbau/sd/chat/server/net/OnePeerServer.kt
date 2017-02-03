package ru.mit.spbau.sd.chat.server.net

import ru.spbau.mit.sd.commons.proto.ServerToPeerMsg
import java.nio.channels.AsynchronousSocketChannel

/**
 * Asynchronous dialogue with one peer.
 *
 * To setup initial IO handlers one must call `setup()` and after work
 * is done `destroy()` call is expected for proper resource release.
 *
 * Every such one peer server has it's owner for proper resource release
 * handling: it is assumed, that eventProcessor knows how to correctly handle
 * server destruction (i.e. connection close). That is needed because
 * one peer connection may be closed either by getting "disconnect" signal
 * from other side of connection, or due to manual (by user) connection
 * accepting server destruction, so...
 */
internal class OnePeerServer(val sock: AsynchronousSocketChannel,
                             val eventProcessor: PeerEventProcessor) {
    fun setup() {

    }

    fun destroy() {
        sock.close()
    }

    fun sendMessage(msg: ServerToPeerMsg) {

    }
}