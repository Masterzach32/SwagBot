package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent
import xyz.swagbot.database.setLastVoiceChannel

object UserJoinEvent : IListener<UserVoiceChannelJoinEvent> {

    override fun handle(event: UserVoiceChannelJoinEvent) {
        if (event.user == event.client.ourUser)
            event.guild.setLastVoiceChannel(event.voiceChannel)
    }
}

object UserLeaveEvent : IListener<UserVoiceChannelLeaveEvent> {

    override fun handle(event: UserVoiceChannelLeaveEvent) {
        if (event.user == event.client.ourUser)
            event.guild.setLastVoiceChannel(null)
    }
}

object UserMovedEvent : IListener<UserVoiceChannelMoveEvent> {

    override fun handle(event: UserVoiceChannelMoveEvent) {
        if (event.user == event.client.ourUser)
            event.guild.setLastVoiceChannel(event.newChannel)
    }
}