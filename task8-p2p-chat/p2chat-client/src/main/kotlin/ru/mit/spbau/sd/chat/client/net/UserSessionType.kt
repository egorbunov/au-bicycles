package ru.mit.spbau.sd.chat.client.net

/**
 * Due to peer2peer architecture users may establish
 * connection in two ways, and we distinguish for current
 * client whenever it connects to other client or if
 * other client connects on it's purpose to this client.
 *
 * Our protocol specifies that Client, which initiates connection
 * must send "DISCONNECT" message at the end of the session.
 */
enum class UserSessionType {
    ESTABLISHED_REMOTELY,
    ESTABLISHED_BY_ME
}