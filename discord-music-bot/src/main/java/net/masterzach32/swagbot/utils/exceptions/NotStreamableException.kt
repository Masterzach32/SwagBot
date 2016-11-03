package net.masterzach32.swagbot.utils.exceptions

class NotStreamableException(val provider: String, val url: String) : Throwable()