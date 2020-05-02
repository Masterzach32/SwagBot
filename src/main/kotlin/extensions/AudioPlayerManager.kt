package xyz.swagbot.extensions

import com.sedmelluq.discord.lavaplayer.player.*
import kotlinx.coroutines.*
import reactor.core.publisher.*

fun AudioPlayerManager.shutdownAsync(): Mono<Void> = Mono.create { emitter ->
    GlobalScope.launch {
        try {
            shutdown()
            emitter.success()
        } catch (e: Throwable) {
            emitter.error(e)
        }
    }
}
