package xyz.swagbot.commands

import net.masterzach32.commands4k.builder.createCommand
import net.masterzach32.timetable.Timetable
import sx.blah.discord.util.EmbedBuilder
import xyz.swagbot.utils.ORANGE
import xyz.swagbot.utils.RED
import java.awt.Color

val LookupCRNCommand = createCommand("Lookup CRN") {
    aliases = listOf("crn")

    helpText {
        description = "Interface with the Virginia Tech Timetable of Classes. (https://hokiespa.vt.edu/)"
        usage["<crn>"] = "Look up all information for the specified CRN."
    }

    onEvent {
        noArgs {
            val embed = EmbedBuilder().withColor(RED)
            return@noArgs builder.withEmbed(embed.withDesc("You must supply a CRN to use this command."))
        }

        all {
            val embed = EmbedBuilder().withColor(RED)
            event.channel.toggleTypingStatus()
            val section = Timetable.crnLookup(args[0], openOnly = false)
            val isOpen = Timetable.crnLookup(args[0]) != null

            if (section == null)
                return@all builder.withEmbed(embed.withDesc("That course does not exist for the current semester."))

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