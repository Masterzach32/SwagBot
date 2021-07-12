package xyz.swagbot.util

import discord4j.core.`object`.entity.*
import discord4j.core.event.domain.interaction.*
import discord4j.core.event.domain.message.*
import io.facet.discord.extensions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

fun Message.onButtonClick(
    scope: CoroutineScope = GlobalScope,
    buttonId: String,
    block: suspend CoroutineScope.(ButtonInteractEvent) -> Unit
) = scope.launch {
    
}

open class ComponentListener(
    private val message: Message,
    private val user: User,
    scope: CoroutineScope
) {

    init {
        scope.launch {
            message.client.flowOf<MessageDeleteEvent>()
                .filter { it.messageId == message.id }
                .onEach { cancel("Search results message was deleted.") }
                .launchIn(this)

            message.componentEvents
                .filterIsNotInitiator()
                .onEach { buttonEvent ->
                    buttonEvent.reply("You cannot use the components on someone else's interaction!")
                        .withEphemeral(true)
                        .await()
                }
                .launchIn(this)

            message.buttonEvents
                .filterIsInitiator()
                .onEach { onButtonClick(it) }
                .launchIn(this)

            message.selectMenuEvents
                .filterIsInitiator()
                .onEach { onSelectChoice(it) }
                .launchIn(this)
        }
    }

    private fun onButtonClick(event: ButtonInteractEvent) {

    }

    private fun onSelectChoice(event: SelectMenuInteractEvent) {

    }

    private fun <T : ComponentInteractEvent> Flow<T>.filterIsInitiator(): Flow<T> =
        filter { it.interaction.user.id == user.id }

    private fun <T : ComponentInteractEvent> Flow<T>.filterIsNotInitiator(): Flow<T> =
        filter { it.interaction.user.id != user.id }
}