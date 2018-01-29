package xyz.swagbot.events

import sx.blah.discord.api.IShard
import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.shard.ReconnectFailureEvent
import xyz.swagbot.utils.ExitCode
import xyz.swagbot.utils.shutdown
import java.lang.Thread.sleep

object ShardDisconnectHandler : IListener<ReconnectFailureEvent> {

    override fun handle(event: ReconnectFailureEvent) {
        if (event.isShardAbandoned) {
            if (!event.client.shards.any { it.isLoggedIn })
                shutdown(event.client, ExitCode.CONNECT_FAILURE)
            else
                reconnectAfterTimeout(event.shard)
        }
    }

    private fun reconnectAfterTimeout(shard: IShard) {
        Thread {
            sleep(240*1000)
            shard.login()
        }.start()
    }
}