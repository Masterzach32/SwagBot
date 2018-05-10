package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent
import sx.blah.discord.handle.obj.ActivityType
import xyz.swagbot.database.getGameSwitcherEntries
import xyz.swagbot.database.isGameSwitcherEnabled
import xyz.swagbot.dsl.getConnectedVoiceChannel
import xyz.swagbot.dsl.isOnVoice

object UserStatusListener : IListener<PresenceUpdateEvent> {

    override fun handle(event: PresenceUpdateEvent) {
        if (event.user.isBot)
            return

        if (event.user.isOnVoice() &&
                event.newPresence.activity.isPresent &&
                event.newPresence.activity.get() == ActivityType.PLAYING &&
                event.newPresence.text.isPresent) {
            val guild = event.user.getConnectedVoiceChannel()!!.guild

            if (guild.isGameSwitcherEnabled()) {
                val map = guild.getGameSwitcherEntries()

                val toMove = map.entries.firstOrNull { it.key == event.newPresence.text.get() }?.value

                if (toMove != null)
                    event.user.moveToVoiceChannel(toMove)
            }
        }
    }
}