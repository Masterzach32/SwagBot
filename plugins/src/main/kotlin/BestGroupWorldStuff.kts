import net.masterzach32.commands4k.AdvancedMessageBuilder
import net.masterzach32.commands4k.Permission
import sx.blah.discord.handle.obj.Permissions
import sx.blah.discord.util.EmbedBuilder
import sx.blah.discord.util.RequestBuffer
import xyz.swagbot.Stats
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.BLUE

createPlugin {
    name = "Best Group World Commands"
    description = "Commands for Best Group World."
    version = "1.0"

    newCommand("Arrest User") {
        aliases = listOf("arrest")

        botPerm = Permission.MOD

        helpText {
            description = "Add the arrested role to a user and remove all other roles they currently have for a period of time."
            usage["<user>"] = "Arrests the user for 1 hour."
            usage["<user> <duration>"] = "Arrests the user for the specified amount of time (in hours)."
        }

        onEvent {
            guild {
                val embed = EmbedBuilder().withColor(BLUE)

                val role = event.client.getRoleByID(470295092528414755)!!

                val user = event.message.mentions.first()
                if (user.longID == 97341976214511616) {
                    embed.withDesc("Sorry, but this user cannot be arrested.")
                    return@guild builder.withEmbed(embed)
                }
                val oldRoles = user.getRolesForGuild(event.guild).filter { it != event.guild.everyoneRole }
                oldRoles.forEach { RequestBuffer.request { user.removeRole(it) } }
                RequestBuffer.request { user.addRole(role) }

                val duration = if (args.size == 2) args[1].toDouble() else 1.0

                embed.withDesc("**${user.getDisplayName(event.guild)}**, you have been arrested for **$duration** hour(s).")

                Thread {
                    Thread.sleep((1000 * 60 * 60 * duration).toLong())
                    RequestBuffer.request { user.removeRole(role) }
                    oldRoles.forEach { RequestBuffer.request { user.addRole(it) } }

                    val builder2 = AdvancedMessageBuilder(event.channel)
                    val embed2 = EmbedBuilder().withColor(BLUE)

                    embed2.withDesc("**${user.mention()}**, you are now free.")

                    RequestBuffer.request { builder2.withEmbed(embed2).build() }
                }.start()

                Stats.USERS_ARRESTED.addStat()
                return@guild builder.withEmbed(embed)
            }
        }
    }

    newCommand("Delete Leo") {
        aliases = listOf("delete")

        botPerm = Permission.ADMIN
        discordPerms = listOf(Permissions.BAN)

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


}