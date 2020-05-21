package xyz.swagbot.features.autoroles

import discord4j.core.event.domain.guild.*
import io.facet.discord.event.*
import io.facet.discord.extensions.*
import xyz.swagbot.*

class AARListener(val feature: AutoAssignRole) : Listener<MemberJoinEvent> {

    override suspend fun on(event: MemberJoinEvent) {
        if (event.member.isBot)
            return

        val roles = feature.autoAssignedRolesFor(event.guildId)
        logger.info("Adding roles: $roles to user: ${event.member.id}")

        roles
            .map { event.member.addRole(it, "Auto assigned role") }
            .forEach { it.await() }
    }
}
