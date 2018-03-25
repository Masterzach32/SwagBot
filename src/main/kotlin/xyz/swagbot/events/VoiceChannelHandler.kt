package xyz.swagbot.events

import sx.blah.discord.api.events.IListener
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelJoinEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelLeaveEvent
import sx.blah.discord.handle.impl.events.guild.voice.user.UserVoiceChannelMoveEvent
import xyz.swagbot.database.getAudioHandler
import xyz.swagbot.database.setLastVoiceChannel

object UserJoinEvent : IListener<UserVoiceChannelJoinEvent> {

    override fun handle(event: UserVoiceChannelJoinEvent) {
        if (event.user == event.client.ourUser)
            event.guild.setLastVoiceChannel(event.voiceChannel)
        else {
            if (event.voiceChannel.usersHere.any { !it.isBot } &&
                    event.voiceChannel.usersHere.contains(event.client.ourUser) &&
                    event.guild.getAudioHandler().shouldAutoplay &&
                    event.guild.getAudioHandler().getQueue().isEmpty() &&
                    event.guild.getAudioHandler().player.playingTrack == null) {
                event.guild.getAudioHandler().getAndQueueAutoplayTrack()
            }
        }
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