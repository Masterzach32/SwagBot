package xyz.swagbot.commands.admin

import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Command
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.handle.obj.IVoiceChannel
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.commands.getWrongArgumentsMessage
import xyz.swagbot.database.addGameSwitcherEntry
import xyz.swagbot.database.getGameSwitcherEntries
import xyz.swagbot.database.removeGameSwitcherEntry
import xyz.swagbot.database.setGameSwitcher
import xyz.swagbot.utils.BLUE
import xyz.swagbot.utils.RED
import xyz.swagbot.utils.getContent

object GameSwitchCommand : Command(
        "Game Switcher",
        "gameswitcher",
        "gs",
        scope = Scope.GUILD,
        botPerm = Permission.ADMIN,
        discordPerms = listOf(Permissions.MANAGE_SERVER)
) {

    init {
        help.desc = "Automatically move users to a voice channel based on the current game they are playing!"
        help.usage["<enable / disable>"] = "Enable or disable this feature."
        help.usage["list"] = "List all registered games and their respective voice channels."
        help.usage["add <game> | <voice channel name / id>"] = "Registers or edits a game / voice channel" +
                " pair with the bot. NOTE: Game name is **case-sensitive**."
        help.usage["remove <game>"] = "Stop automatically moving users who are playing the specified game."
    }

    override fun execute(cmdUsed: String, args: Array<String>, event: MessageReceivedEvent,
                         builder: AdvancedMessageBuilder): AdvancedMessageBuilder {
        if(args.isEmpty())
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        val embed = EmbedBuilder().withColor(BLUE)
        val command = args[0]
        if (command == "enable") {
            event.guild.setGameSwitcher(true)
            embed.withDesc("You have enabled voice-channel switching based on your detected game!")
            if(event.guild.getGameSwitcherEntries().isEmpty())
                embed.appendDesc("\nNow you can add some games by using `~game add <game> | " +
                        "<voice channel name or id>`")
        } else if (command == "disable") {
            event.guild.setGameSwitcher(false)
            embed.withDesc("Disabled voice channel switching.")
        } else if (command == "list") {
            val entries = event.guild.getGameSwitcherEntries()
            val set = mutableSetOf<IVoiceChannel>()
            entries.forEach { set.add(it.value) }

            set.forEach { vc ->
                val games = entries.filter { it.value == vc }.map { it.key }
                var list = ""
                games.forEach { list += "$it\n" }
                embed.appendField(vc.name, list, true)
            }

            embed.withTitle("Game Switcher Entries")
        } else if (command == "add") {
            for(i in args.indices) {
                if(args[i] == "|") {
                    val game = getContent(args, 1, i)
                    val vc = event.guild.voiceChannels.firstOrNull { it.name == getContent(args, i + 1) } ?: try {
                        event.guild.getVoiceChannelByID(getContent(args, i + 1).toLong())
                    } catch (t: Throwable) {
                        return builder.withEmbed(embed.withColor(RED).withDesc("Could not find voice channel: " +
                                "**${getContent(args, i + 1)}**"))
                    }

                    event.guild.addGameSwitcherEntry(game, vc)
                    embed.withDesc("Added / Edited game trigger: **$game** assigned to **$vc**.")
                    break
                }
            }
        } else if (command == "remove") {
            val game = getContent(args, 1, args.size)
            val register = event.guild.removeGameSwitcherEntry(game)
            if (register != null)
                embed.withDesc("Players running **${register.key}** will no longer be moved " +
                        "to **${register.value.name}**.")
            else
                embed.withColor(RED).withDesc("Could not find a key/value set for **$game**.")
        } else
            return getWrongArgumentsMessage(builder, this, cmdUsed)
        return builder.withEmbed(embed)
    }
}