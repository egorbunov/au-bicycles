package ru.mit.spbau.sd.chat.client

/**
 * Thrown if attempted to add duplicate user to the chat model
 */
class ChatUserAlreadyExists(message: String?) : Exception(message)

/**
 * Thrown if model queried for data related to non-existing inside
 * that model user
 */
class ChatUserDoesNotExists(message: String?) : Exception(message)

/**
 * Error, occurred during the stage of client start
 */
class ClientBootstrapError(message: String?): Exception(message)

/**
 * Error, which is thrown in case error during CONNECT stage of two clients
 */
class ClientHandshakeError(message: String?): Exception(message)