package xyz.swagbot.status

interface StatusMessage {

    fun getMessage(): String?
}

fun StatusMessage(func: () -> String?): StatusMessage {
    return object : StatusMessage {
        override fun getMessage(): String? {
            return func()
        }
    }
}