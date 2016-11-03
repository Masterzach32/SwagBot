package net.masterzach32.swagbot.utils.exceptions

class YouTubeDLException(val url: String, val exitCode: Int) : Throwable("An error occurred downloading " + url)