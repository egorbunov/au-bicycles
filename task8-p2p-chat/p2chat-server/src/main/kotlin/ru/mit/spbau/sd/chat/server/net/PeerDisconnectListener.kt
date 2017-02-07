package ru.mit.spbau.sd.chat.server.net

interface PeerDisconnectListener <in T> {
    fun peerDisconnected(peer: T)
}