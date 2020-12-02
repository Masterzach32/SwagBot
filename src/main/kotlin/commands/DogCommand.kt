package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
import io.facet.core.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import xyz.swagbot.util.*

object DogCommand : ChatCommand(
    name = "Dog Pictures",
    aliases = setOf("dog", "randomdog")
) {

    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = JacksonSerializer()
        }
    }

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            val channel = getChannel()

            launch { channel.type().await() }

            val response: Response = try {
                retry(3, 3000) {
                    httpClient.get("https://dog.ceo/api/breeds/image/random")
                }
            } catch (e: Throwable) {
                message.reply(errorTemplate("Sorry, but i'm having trouble getting images at the moment.", e))
                return@runs
            }

            if (response.status == "success") {
                val image = response.getImage()
                message.reply {
                    file(response.fileName, image)
                }
            } else {
                message.reply("Sorry, but i'm having trouble getting images at the moment.")
            }
        }

        argument("breed", greedyString()) {
            runs {

            }
        }
    }

    private data class Response(val message: String, val status: String) {

        val fileName: String
            get() = message.split("/").last()

        suspend fun getImage() = retry(3, 3000) {
            httpClient.get<HttpResponse>(message).content.toInputStream()
        }
    }
}
