package xyz.swagbot.util

import kotlinx.coroutines.*

suspend inline fun <T> retry(n: Int, errorDelayMillis: Long, fn: (counter: Int) -> T): T {
    lateinit var ex: Exception
    repeat(n) { counter ->
        try { return fn(counter) }
        catch (e: Exception) {
            if (counter < n-1)
                delay((counter+1)*errorDelayMillis)
            ex = e
        }
    }
    throw ex
}
