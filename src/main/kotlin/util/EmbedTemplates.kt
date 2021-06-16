package xyz.swagbot.util

import io.facet.discord.dsl.*

val baseTemplate: EmbedTemplate = embed {
    color = BLUE
}

val errorTemplate: EmbedTemplate = embed {
    color = RED
}

fun errorTemplate(description: String, throwable: Throwable) = errorTemplate.andThen {
    this.description = description
    throwable::class.simpleName?.let { exceptionName ->
        field("Exception", exceptionName, true)
    }
    if (throwable.localizedMessage.isNotEmpty())
        field("Message", throwable.localizedMessage, true)
}