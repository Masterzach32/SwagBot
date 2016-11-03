package net.masterzach32.swagbot.utils.exceptions

import java.io.File

class FFMPEGException(val file: File, val url: String, val exitCode: Int) : Throwable()