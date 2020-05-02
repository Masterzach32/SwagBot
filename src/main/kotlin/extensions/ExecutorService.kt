package xyz.swagbot.extensions

import kotlinx.coroutines.*
import reactor.core.publisher.*
import java.util.concurrent.*

fun ExecutorService.shutdownAsync(): Mono<Void> = Mono.create { emitter ->
    GlobalScope.launch {
        try {
            shutdown()
            emitter.success()
        } catch (e: Throwable) {
            emitter.error(e)
        }
    }
}