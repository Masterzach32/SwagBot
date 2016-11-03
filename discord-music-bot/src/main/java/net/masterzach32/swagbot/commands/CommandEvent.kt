package net.masterzach32.swagbot.commands

import com.mashape.unirest.http.exceptions.UnirestException
import sx.blah.discord.handle.obj.IMessage
import sx.blah.discord.util.DiscordException
import sx.blah.discord.util.MissingPermissionsException
import sx.blah.discord.util.RateLimitException

import java.io.IOException

interface CommandEvent {

    @Throws(RateLimitException::class, MissingPermissionsException::class, DiscordException::class, UnirestException::class, IOException::class)
    fun execute(message: IMessage, params: Array<String>)

}