package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent
import xyz.swagbot.database.lastVoiceChannel
import xyz.swagbot.database.trackHandler

object UserJoinEvent : IListener<UserVoiceChannelJoinEvent> {

    override fun handle(event: UserVoiceChannelJoinEvent) {
        if (event.user == event.client.ourUser)
            event.guild.lastVoiceChannel = event.voiceChannel
        else {
            val usersHere = event.voiceChannel.usersHere
            val trackHandler = event.guild.trackHandler
            if (usersHere.any { !it.isBot } &&
                    usersHere.contains(event.client.ourUser) &&
                    trackHandler.shouldAutoplay &&
                    trackHandler.getQueue().isEmpty() &&
                    trackHandler.player.playingTrack == null
            ) {
                event.guild.trackHandler.playNext()
            }
        }
    }
}

object UserLeaveEvent : IListener<UserVoiceChannelLeaveEvent> {

    override fun handle(event: UserVoiceChannelLeaveEvent) {
        if (event.user == event.client.ourUser)
            event.guild.lastVoiceChannel = null
    }
}

object UserMovedEvent : IListener<UserVoiceChannelMoveEvent> {

    override fun handle(event: UserVoiceChannelMoveEvent) {
        if (event.user == event.client.ourUser)
            event.guild.lastVoiceChannel = event.newChannel
    }
}