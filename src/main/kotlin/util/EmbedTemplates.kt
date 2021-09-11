package xyz.swagbot.util

import discord4j.core.spec.EmbedCreateSpec
import io.facet.common.dsl.and
import io.facet.common.dsl.embed

val baseTemplate: EmbedCreateSpec = embed {
    color = BLUE
}

val errorTemplate: EmbedCreateSpec = embed {
    color = RED
}

fun errorTemplate(description: String, throwable: Throwable) = errorTemplate.and {
    this.description = description
    throwable::class.simpleName?.let { exceptionName ->
        field("Exception", exceptionName, true)
    }
    if (throwable.localizedMessage.isNotEmpty())
        field("Message", throwable.localizedMessage, true)
}