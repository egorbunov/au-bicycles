package ru.mir.spbau.sd.chat.client

class ChatUserAlreadyExists(message: String?) : Exception(message)
class ChatUserDoesNotExists(message: String?) : Exception(message)
