package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent
import sx.blah.discord.handle.obj.ActivityType
import xyz.swagbot.database.getGameSwitcherEntries
import xyz.swagbot.database.isGameSwitcherEnabled

object UserStatusListener : IListener<PresenceUpdateEvent> {

    override fun handle(event: PresenceUpdateEvent) {
        if (event.user.isBot)
            return

        if (event.user.voiceStates.keySet().isNotEmpty() &&
                event.newPresence.activity.isPresent &&
                event.newPresence.activity.get() == ActivityType.PLAYING) {
            val guild = event.client.getGuildByID(event.user.voiceStates.keySet().first())

            if (guild.isGameSwitcherEnabled()) {
                val map = guild.getGameSwitcherEntries()

                val toMove = map.entries.firstOrNull { it.key == event.newPresence.text.get() }?.value

                if (toMove != null)
                    event.user.moveToVoiceChannel(toMove)
            }
        }
    }
}