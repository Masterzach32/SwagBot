import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Permission
import org.jetbrains.exposed.sql.select
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.category.CategoryDeleteEvent
import sx.blah.discord.handle.impl.events.guild.voice.VoiceChannelDeleteEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import xyz.swagbot.database.*
import xyz.swagbot.database.TempChannels
import xyz.swagbot.dsl.privateChannel
import xyz.swagbot.dsl.request
import xyz.swagbot.dsl.requestGet
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.*
import java.time.Instant
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CheckTempChannelsTask(val client: IDiscordClient) : Runnable {

    override fun run() {
        try {
            sql {
                TempChannel.all().asSequence()
                        .map { it to client.getVoiceChannelByID(it.channelId) }
                        .forEach {
                            if (it.second.connectedUsers.isNotEmpty()) {
                                if (it.first.timerStart != null) {
                                    xyz.swagbot.logger.info("Canceling timeout for temp channel ${it.second.longID}")
                                    it.first.timerStart = null
                                }
                            } else if (it.first.timerStart == null) {
                                xyz.swagbot.logger.info("Beginning timeout for temp channel ${it.second.longID}")
                                it.first.timerStart = Instant.now().epochSecond
                            } else if (Instant.now().epochSecond - it.first.timerStart!! > 5 * 60) {
                                xyz.swagbot.logger.info("Deleting temp channel ${it.second.longID}")
                                request { it.second.delete() }
                                request { sendTimeoutMessage(client, it.first.ownerId, it.second) }
                            }
                        }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendTimeoutMessage(client: IDiscordClient, userId: Long, channel: IVoiceChannel) {
        AdvancedMessageBuilder(client.getUserByID(userId).privateChannel)
                .withEmbed(embedRed("Your temporary channel, **${channel.name}**, has expired in **${channel.guild.name}**"))
                .build()
    }
}

createPlugin {
    name = "Temporary Voice Channels"
    description = "Allow users to create temporary voice channels."
    version = "1.0"

    newCommand("Toggle Temporary Voice Channels") {
        aliases = listOf("tempvoicetoggle", "temptoggle")

        botPerm = Permission.ADMIN

        helpText {
            description = "Toggle whether server users can create temporary voice channels using ~tempchannel like in teamspeak."
        }

        onEvent {
            guild {
                val embed = embedBlue()

                if (!event.guild.areTempChannelsEnabled) {
                    event.guild.tempChannelCategory = requestGet { event.guild.createCategory("Temporary Channels") }
                    embed.withDesc("Users can now create temporary voice channels using `${event.guild.commandPrefix}tempchannel`.")
                } else {
                    requestGet { event.guild.tempChannelCategory?.delete() }
                    event.guild.tempChannelCategory = null
                    embed.withDesc("Users can no longer create temporary voice channels.")
                }

                return@guild builder.withEmbed(embed)
            }
        }
    }

    newCommand("Temporary Voice Channels") {
        aliases = listOf("tempchannel", "createchannel")

        helpText {
            description = "Create temporary voice channels."
        }

        onEvent {
            guild {
                if (!event.guild.areTempChannelsEnabled)
                    return@guild null

                val embed = if (event.guild.getTempChannelForUser(event.author) == null) {
                    val channel = requestGet { event.guild.createTempChannel(getContent(args, 0), event.author) }
                    channel.changeCategory(event.guild.tempChannelCategory)
                    channel.overrideUserPermissions(
                            event.author,
                            EnumSet.of(Permissions.MANAGE_CHANNEL, Permissions.MANAGE_PERMISSIONS),
                            EnumSet.noneOf(Permissions::class.java)
                    )
                    embedBlue("Created new voice channel: **${channel.name}**.")
                } else {
                    embedRed("You already have a temporary voice channel in this server!")
                }

                return@guild builder.withEmbed(embed)
            }
        }
    }

    newListener<ReadyEvent> {
        sql {
            TempChannel.all().asSequence()
                    .filter { requestGet { client.getChannelByID(it.channelId) } == null }
                    .forEach { it.delete() }

            Guilds.select { Guilds.temp_category.isNotNull() }.asSequence()
                    .map { it to requestGet { client.getCategoryByID(it[Guilds.temp_category]!!) } }
                    .filter { it.second == null }
                    .forEach { it.first[Guilds.temp_category] = null }
        }

        Executors.newScheduledThreadPool(1) { Thread("TempChannels Watcher") { it.run() } }
                .apply { scheduleAtFixedRate(CheckTempChannelsTask(client), 1, 1, TimeUnit.MINUTES) }
                .also { addShutdownHook { it.shutdown() } }
    }

    newListener<CategoryDeleteEvent> {
        if (category.longID == sql { xyz.swagbot.database.Guilds.select { Guilds.id eq guild.longID }.first()[Guilds.temp_category] }) {
            guild.tempChannelCategory = null
            guild.getTempChannels().forEach { request { it.delete() } }
        }
    }

    newListener<VoiceChannelDeleteEvent> {
        sql { TempChannel.find { TempChannels.channel_id eq voiceChannel.longID }.firstOrNull()?.delete() }
    }
}