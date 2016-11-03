package net.masterzach32.swagbot.utils.exceptions

class YouTubeAPIException(url: String) : Throwable("Could not parse API call on " + url)
