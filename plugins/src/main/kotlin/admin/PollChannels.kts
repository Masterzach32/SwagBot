import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import sx.blah.discord.handle.impl.events.guild.channel.ChannelDeleteEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IChannel
import sx.blah.discord.handle.obj.Permissions
import xyz.swagbot.Stats
import xyz.swagbot.database.commandPrefix
import xyz.swagbot.database.create
import xyz.swagbot.database.sql
import xyz.swagbot.dsl.*
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.embedBlue
import xyz.swagbot.utils.listOfEmojis
import java.util.*

createPlugin {
    name = "Poll Channels"
    description = "Create channels for user polls."
    version = "1.0"

    val PollChannels = object : Table("sb_poll_channels") {
        val channel_id = long("channel_id").primaryKey()

        fun exists(channel: IChannel): Boolean {
            return sql { select { channel_id eq channel.longID }.firstOrNull() != null }
        }

        fun delete(channel: IChannel) {
            sql { deleteWhere { channel_id eq channel.longID } }
        }
    }

    sql { create(PollChannels) }

    newCommand("Poll") {
        aliases = listOf("polltoggle")

        botPerm = Permission.MOD
        discordPerms = listOf(Permissions.MANAGE_CHANNEL)

        helpText {
            description = "Designate a text channel as a poll channel. All messages are appended with the thumbs up, " +
                    "thumbs down, and shrug emoji."
        }

        onEvent {
            guild {
                val embed = embedBlue()
                if (PollChannels.exists(event.channel)) {
                    sql { PollChannels.delete(event.channel) }
                    request { event.channel.removePermissionsOverride(event.guild.everyoneRole) }
                    embed.withDesc("This channel has been toggled off for polling.")
                } else {
                    sql { PollChannels.insert { it[channel_id] = event.channel.longID } }
                    request {
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

            val emojis = listOfEmojis("thumbsup", "thumbsdown", "shrug")

            listen<MessageReceivedEvent> {
                if (!author.isBot && PollChannels.exists(channel) && !message.content.startsWith(guild.commandPrefix)) {
                    emojis.forEach { requestGet { message.addReaction(it) } }
                    Stats.POLLS_CREATED.addStat()
                }
            }

            listen<ChannelDeleteEvent> {
                if (PollChannels.exists(channel))
                    sql { PollChannels.delete(channel) }
            }
        }
    }
}