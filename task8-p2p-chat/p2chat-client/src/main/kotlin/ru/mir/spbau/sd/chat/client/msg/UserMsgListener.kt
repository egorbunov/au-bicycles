package ru.mir.spbau.sd.chat.client.msg

import ru.spbau.mit.sd.commons.proto.PeerToPeerMsg

/**
 * One, who listens to messages, which come from one user connection
 */
interface UserMsgListener {
    fun gotMessage(message: PeerToPeerMsg)
}