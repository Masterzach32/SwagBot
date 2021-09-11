package xyz.swagbot.util

import discord4j.core.`object`.entity.Message
import discord4j.core.`object`.entity.User
import discord4j.core.event.domain.interaction.ButtonInteractEvent
import discord4j.core.event.domain.interaction.ComponentInteractEvent
import discord4j.core.event.domain.interaction.SelectMenuInteractEvent
import discord4j.core.event.domain.message.MessageDeleteEvent
import io.facet.common.await
import io.facet.common.componentEvents
import io.facet.common.flowOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ComponentListener(
    private val message: Message,
    private val user: User,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : CoroutineScope by scope {

    val componentEvents: Flow<ComponentInteractEvent> = message.componentEvents
    val buttonEvents: Flow<ButtonInteractEvent> = componentEvents.filterIsInstance()
    val selectMenuEvents: Flow<SelectMenuInteractEvent> = componentEvents.filterIsInstance()

    init {
        message.client.flowOf<MessageDeleteEvent>()
            .filter { it.messageId == message.id }
            .onEach { cancel("Search results message was deleted.") }
            .launchIn(this)

        componentEvents
            .filterIsNotInitiator()
            .onEach { buttonEvent ->
                buttonEvent.reply("You cannot use the components on someone else's interaction!")
                    .withEphemeral(true)
                    .await()
            }
            .launchIn(this)
    }

    inline fun <reified E : ComponentInteractEvent> on(
        componentId: String,
        crossinline block: suspend CoroutineScope.(E) -> Unit
    ) = launch {
        componentEvents
            .filterIsInstance<E>()
            .filterIsInitiator()
            .filter { it.customId == componentId }
            .onEach { block(it) }
            .collect()
    }

    fun onButton(
        componentId: String,
        block: suspend CoroutineScope.(ButtonInteractEvent) -> Unit
    ) = launch {
        buttonEvents
            .filterIsInitiator()
            .filter { it.customId == componentId }
            .onEach { block(it) }
            .collect()
    }

    fun onSelect(
        componentId: String,
        block: suspend CoroutineScope.(SelectMenuInteractEvent) -> Unit
    ) = launch {
        selectMenuEvents
            .filterIsInitiator()
            .filter { it.customId == componentId }
            .onEach { block(it) }
            .collect()
    }

    fun <T : ComponentInteractEvent> Flow<T>.filterIsInitiator(): Flow<T> =
        filter { it.interaction.user.id == user.id }

    fun <T : ComponentInteractEvent> Flow<T>.filterIsNotInitiator(): Flow<T> =
        filter { it.interaction.user.id != user.id }
}