package xyz.swagbot.commands.mod

import net.masterzach32.commands4k.*
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.utils.RED

object MassAfkCommand : Command("Mass AFK", "massafk", "mafk", botPerm = Permission.MOD,
        scope = Command.Scope.GUILD, discordPerms = listOf(Permissions.VOICE_MOVE_MEMBERS)) {

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder? {

        val embed = EmbedBuilder().withColor(RED)

        val afkChannel = event.guild.afkChannel ?:
                return builder.withEmbed(embed.withDesc("This guild does not have an afk channel."))

        event.guild.users
                .filter { it != event.client.ourUser && it.getVoiceStateForGuild(event.guild).channel != null }
                .forEach { RequestBuffer.request { it.moveToVoiceChannel(afkChannel) } }

        return null
    }

    override fun getCommandHelp(usage: MutableMap<String, String>) {

    }
}