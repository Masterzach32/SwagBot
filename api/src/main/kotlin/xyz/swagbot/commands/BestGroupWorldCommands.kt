package xyz.swagbot.commands

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Permission
import net.masterzach32.commands4k.builder.createCommand
import org.jetbrains.exposed.sql.*
import sx.blah.discord.api.IDiscordClient
import sx.blah.discord.handle.impl.events.ReadyEvent
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.impl.events.user.PresenceUpdateEvent
import sx.blah.discord.handle.obj.*
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.Stats
import xyz.swagbot.database.create
import xyz.swagbot.database.sql
import xyz.swagbot.dsl.privateChannel
import xyz.swagbot.dsl.removeRoles
import xyz.swagbot.dsl.request
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.embedBlue
import xyz.swagbot.utils.embedRed
import xyz.swagbot.utils.getContent
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

val GUILD_ID = 97342233241464832L
val arrestedRoleId = 470295092528414755L

object ArrestedUsers : Table("sb_arrested_users") {
    val convictionId = integer("conviction_id").primaryKey().autoIncrement()
    val guildId = long("guild_id")
    val userId = long("user_id")
    val arrestedBy = long("arrested_by_id")
    val convictedAt = long("convicted_at")
    val duration = long("duration")
    val roles = text("roles")
    val reason = text("reason")
    val released = bool("released").default(false)

    fun hasRecord(user: IUser) = sql { select { userId eq user.longID }.firstOrNull() != null }

    fun isArrested(user: IUser) = sql { select { userId eq user.longID }.any { !it[released] } }

    fun addNewSentance(guild: IGuild, arrested: IUser, arrestedBy: IUser, duration: Double, reason: String) {
        val currentlyArrested = isArrested(arrested)
        sql {
            insert {
                it[this.guildId] = guild.longID
                it[this.userId] = arrested.longID
                it[this.arrestedBy] = arrestedBy.longID
                it[this.convictedAt] = Instant.now().epochSecond
                it[this.duration] = (duration * 60 * 60).toLong()
                it[this.roles] = arrested.getRolesForGuild(arrested.client.getGuildByID(GUILD_ID)).toList()
                        .map { it.longID }
                        .toMutableList().apply { remove(GUILD_ID) }
                        .run { toString().drop(1).dropLast(1).replace(" ", "") }
                it[this.reason] = reason
            }
        }

        if (!currentlyArrested) {
            Stats.USERS_ARRESTED.addStat()
            val oldRoles = arrested.getRolesForGuild(guild).apply { remove(guild.everyoneRole) }
            request { arrested.removeRoles(oldRoles) }
            request { arrested.addRole(guild.client.getRoleByID(arrestedRoleId)) }
        }
        Stats.USERS_CONVICTED.addStat()

        request { arrested.privateChannel.sendMessage(getArrestNotice(arrestedBy, guild, duration, reason).build()) }
    }

    fun getUsers(guild: IGuild) = sql { selectAll().mapNotNull { guild.getUserByID(it[userId]) } }

}

class JailTimeCheckerTask(val client: IDiscordClient, val roleId: Long) : Runnable {

    override fun run() {
        sql {
            ArrestedUsers.select { ArrestedUsers.released eq false }
                    .filter { Instant.now().epochSecond - it[ArrestedUsers.convictedAt] > it[ArrestedUsers.duration] }
                    .map { it to client.getUserByID(it[ArrestedUsers.userId]) }
                    .forEach { (row, user) ->
                        request { user.removeRole(client.getRoleByID(roleId)) }
                        row[ArrestedUsers.roles]
                                .split(",")
                                .mapNotNull { client.getRoleByID(it.toLong()) }
                                .forEach { request { user.addRole(it) } }

                        val builder = AdvancedMessageBuilder(user.privateChannel)
                        request {
                            builder.withEmbed(
                                    embedBlue("**${user.name}**, you are now free.").withTimestamp(Instant.now())
                            ).build()
                        }

                        ArrestedUsers.update({ ArrestedUsers.convictionId eq row[ArrestedUsers.convictionId] }) {
                            it[ArrestedUsers.released] = true
                        }
                    }
        }
    }
}

val ArrestCommand = createCommand("Arrest User") {
    aliases = listOf("arrest")

    botPerm = Permission.MOD
    hidden = true

    helpText {
        description = "Add the arrested role to a user and remove all other roles they currently have for a period of time."
        usage["<user> <duration> <reason>"] = "Arrests the user for the specified amount of time (in hours) with reason."
    }

    onEvent {
        guild {
            if (event.guild.longID != GUILD_ID)
                return@guild null

            val role = event.client.getRoleByID(arrestedRoleId)!!

            if (args.isNotEmpty()) {
                val user = event.message.mentions.firstOrNull()
                if (user == event.client.applicationOwner || user == event.client.ourUser)
                    return@guild builder.withEmbed(embedRed("Sorry, but this user cannot be arrested."))

                if (user != null && args.size >= 2) {
                    val duration = try {
                        args[1].toDouble()
                    } catch (e: Exception) {
                        1.0
                    }
                    if (args.size >= 3) {
                        val reason = getContent(args, 2)
                        ArrestedUsers.addNewSentance(event.guild, user, event.author, duration, reason)

                        request {
                            builder.withEmbed(
                                    embedBlue("${user.getDisplayName(event.guild)} has been arrested. Use the " +
                                            "`~viewarrests` command to see more info.")
                            ).build()
                        }
                    } else {
                        return@guild builder.withEmbed(embedRed("You must specify a reason to arrest a user."))
                    }
                } else {
                    return@guild builder.withEmbed(embedRed("You must specify the duration to arrest a user."))
                }
            } else {
                return@guild builder.withEmbed(embedRed("You must specify the user to be arrested by mentioning them."))
            }

            return@guild null
        }

        listen<ReadyEvent> {
            sql { create(ArrestedUsers) }

            Executors.newScheduledThreadPool(1).scheduleAtFixedRate(JailTimeCheckerTask(client, arrestedRoleId), 1, 5, TimeUnit.MINUTES)
        }

        /*listen<PresenceUpdateEvent> {
            val autoArrestReason = "AUTO ARRESTED FOR RUNNING GAME: \"Fortnite\""
            val ourGuild = client.getGuildByID(GUILD_ID)
            if (ourGuild.users.contains(user) && newPresence.text.orElse("") == "Fortnite")
                ArrestedUsers.addNewSentance(ourGuild, user, client.ourUser, 1.0, autoArrestReason)
        }

        listen<MessageReceivedEvent> {
            val autoArrestReason = "AUTO ARRESTED FOR MENTIONING WORD: \"Fortnite\""
            val ourGuild = client.getGuildByID(GUILD_ID)
            if (guild == ourGuild && message.content.toLowerCase().contains("fortnite"))
                ArrestedUsers.addNewSentance(ourGuild, author, client.ourUser, 1.0, autoArrestReason)
        }*/
    }
}

val PardonCommand = createCommand("Pardon User") {
    aliases = listOf("pardon")

    botPerm = Permission.MOD
    hidden = true

    onEvent {
        guild {
            val arrestedRole = event.client.getRoleByID(arrestedRoleId)

            if (args.isNotEmpty()) {
                val user = event.message.mentions.firstOrNull()
                if (user != null) {
                    val wasArrested = sql {
                        return@sql ArrestedUsers
                                .select { ArrestedUsers.userId.eq(user.longID) and ArrestedUsers.guildId.eq(event.guild.longID) }
                                .firstOrNull() != null
                    }

                    if (wasArrested) {
                        request { user.removeRole(arrestedRole) }
                        sql {
                            ArrestedUsers
                                    .select { ArrestedUsers.userId.eq(user.longID) and ArrestedUsers.guildId.eq(event.guild.longID) }
                                    .first()[ArrestedUsers.roles]
                                    .split(",")
                                    .mapNotNull { event.client.getRoleByID(it.toLong()) }
                                    .forEach { request { user.addRole(it) } }

                            ArrestedUsers.deleteWhere {
                                ArrestedUsers.userId.eq(user.longID) and ArrestedUsers.guildId.eq(event.guild.longID)
                            }
                        }
                        return@guild builder.withEmbed(embedBlue("**${user.getDisplayName(event.guild)}** has been pardoned."))
                    } else {
                        return@guild builder.withEmbed(embedRed("**${user.getDisplayName(event.guild)}** is not currently under arrest."))
                    }
                } else {
                    return@guild builder.withEmbed(embedRed("Could not find that user."))
                }
            } else {
                return@guild builder.withEmbed(embedRed("You must specify the user to be pardoned by mentioning them."))
            }
        }
    }
}

val DeleteUserCommand = createCommand("Delete Leo") {
    aliases = listOf("delete")

    botPerm = Permission.ADMIN
    discordPerms = listOf(Permissions.BAN)
    hidden = true

    helpText {
        description = "Deletes a user. Only works on Leo."
    }

    onEvent {
        guild {
            val embed = EmbedBuilder().withColor(BLUE)

            return@guild builder.withEmbed(embed)
        }
    }
}

private fun getArrestNotice(arrestedBy: IUser, guild: IGuild, duration: Double, reason: String): EmbedBuilder {
    return embedBlue()
            .withTitle("Conviction Notice")
            .withDesc("You have been arrested in **${guild.name}**.")
            .appendDesc("\n\n**Sentence Information:**")
            .appendDesc("\nArrested By: ${arrestedBy.name}#${arrestedBy.discriminator}")
            .appendDesc("\nReason: $reason")
            .appendDesc("\nRelease Time: ${
            DateTimeFormatter
                    .ofLocalizedDateTime(FormatStyle.MEDIUM)
                    .withLocale(Locale.US)
                    .withZone(ZoneId.systemDefault())
                    .format(Instant.now().plusSeconds((duration*60*60).toLong()))
            }")
            .appendDesc("\n\nAll of your roles in ${guild.name} have been removed, and will be reinstated once your ")
            .appendDesc("sentence is complete. ")
            .appendDesc("\n\nIf you believe this was in error, please message one of the moderators to review your ")
            .appendDesc("record. Rules are made to keep the peace. If we find that you continue to break the rules, ")
            .appendDesc("the staff of ${guild.name} reserve the right to take further action.")
            .withTimestamp(Instant.now())
}