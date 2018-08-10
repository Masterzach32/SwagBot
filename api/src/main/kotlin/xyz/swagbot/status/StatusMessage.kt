package xyz.swagbot.status

interface StatusMessage {

    val message: String?
}

fun StatusMessage(func: () -> String?): StatusMessage {
    return object : StatusMessage {
        override val message: String?
            get() = func.invoke()
    }
}