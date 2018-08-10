import net.masterzach32.timetable.Timetable
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.plugins.createPlugin
import xyz.swagbot.utils.ORANGE
import xyz.swagbot.utils.embedRed

createPlugin {
    name = "VT Timetable API Commands"
    description = "Interface with the Virginia Tech Timetable of Classes. (https://hokiespa.vt.edu/)"
    version = "1.0"

    newCommand("Lookup CRN") {
        aliases = listOf("crn")

        helpText {
            description = "Interface with the Virginia Tech Timetable of Classes. (https://hokiespa.vt.edu/)"
            usage["<crn>"] = "Look up all information for the specified CRN."
            usage["<crn> <term>"] = "Look up all information for the specified CRN during the specified term."
        }

        onEvent {
            noArgs {
                return@noArgs builder.withEmbed(embedRed("You must supply a CRN to use this command."))
            }

            all {
                event.channel.toggleTypingStatus()
                val section = Timetable.lookupCrn(args[0], if (args.size == 2) Timetable.Term(args[1]) else Timetable.getCurrentTerm()).block()
                val isOpen = Timetable.lookupCrn(args[0], if (args.size == 2) Timetable.Term(args[1]) else Timetable.getCurrentTerm(), openOnly = true).block() != null

                if (section == null)
                    return@all builder.withEmbed(embedRed("That course does not exist for the current semester."))

                val embed = EmbedBuilder()
                embed.withColor(ORANGE)
                embed.withTitle("${section.subjectCode}-${section.courseNumber} ${section.name}")
                embed.appendField("Instructor", section.instructor, true)
                embed.appendField("Location", section.location, true)
                embed.appendField("Time", "${section.startTime} - ${section.endTime} on ${section.days}", true)
                embed.appendField("Credits", section.credits, true)
                embed.appendField("Capacity", "${section.capacity}", true)
                embed.appendField("Enrollment Status", if (isOpen) "Full" else "Open", true)

                return@all builder.withEmbed(embed)
            }
        }
    }
}