package xyz.swagbot.util

import discord4j.core.`object`.command.*
import discord4j.discordjson.json.*
import discord4j.rest.util.*
import io.facet.core.extensions.*
import kotlin.reflect.*

fun applicationCommand(
    name: String,
    desc: String,
    block: ApplicationCommandConfig.() -> Unit = {}
) = ApplicationCommandRequest.builder()
    .name(name)
    .description(desc)
    .addAllOptions(ApplicationCommandConfig().apply(block).options)
    .build()

class ApplicationCommandConfig {

    val options = mutableListOf<ApplicationCommandOptionData>()

    fun addOption(name: String, desc: String, type: ApplicationCommandOptionType) = options.add(
        ApplicationCommandOptionData.builder()
            .name(name)
            .description(desc)
            .type(type.value)
            .build()
    )
}

inline fun <reified T> ApplicationCommandInteraction.getOption(name: String) {

    T::class.members.filterIsInstance<KProperty<*>>()
}
