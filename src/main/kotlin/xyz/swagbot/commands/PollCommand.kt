package xyz.swagbot.commands

import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.Stats
import xyz.swagbot.database.PollChannels
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.database.sql
import xyz.swagbot.utils.BLUE
import java.util.*

val PollCommand = createCommand("Poll") {
    aliases = listOf("polltoggle")

    botPerm = Permission.MOD
    discordPerms = listOf(Permissions.MANAGE_CHANNEL)

    helpText {
        description = "Designate a text channel as a poll channel. All messages are appended with the thumbs up, " +
                "thumbs down, and shrug emoji."
    }

    onEvent {
        guild {
            val embed = EmbedBuilder().withColor(BLUE)
            if (isPollChannel(event.channel)) {
                sql { PollChannels.deleteWhere { PollChannels.id eq event.channel.longID } }
                RequestBuffer.request { event.channel.removePermissionsOverride(event.guild.everyoneRole) }
                embed.withDesc("This channel has been toggled off for polling.")
            } else {
                sql { PollChannels.insert { it[PollChannels.id] = event.channel.longID } }
                RequestBuffer.request {
                    event.channel.overrideRolePermissions(
                            event.guild.everyoneRole,
                            null,
                            EnumSet.of(Permissions.ADD_REACTIONS)
                    )
                }
                embed.withDesc("This channel has been toggled on for polling.")
            }
            return@guild builder.withEmbed(embed)
        }

        val emojis = listOf(
                "\uD83D\uDC4D", // thumbs up
                "\uD83D\uDC4E", // thumbs down
                "\uD83E\uDD37"  // shrug
        )

        listen<MessageReceivedEvent> {
            if (!author.isBot && isPollChannel(channel) &&
                    !message.content.startsWith(guild.commandPrefix)) {
                emojis.forEach { RequestBuffer.request { message.addReaction(ReactionEmoji.of(it)) }.get() }
                Stats.POLLS_CREATED.addStat()
            }
        }

        listen<ChannelDeleteEvent> {
            if (isPollChannel(channel))
                sql { PollChannels.deleteWhere { PollChannels.id eq channel.longID } }
        }
    }
}

private fun isPollChannel(channel: IChannel): Boolean = sql {
    PollChannels.select { PollChannels.id eq channel.longID }.firstOrNull() != null
}