package xyz.swagbot.commands

import com.mojang.brigadier.arguments.StringArgumentType.*
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

            channel.type().async()

            val response: Response = try {
                retry(3, 3000) {
                    httpClient.get("https://dog.ceo/api/breeds/image/random")
                }
            } catch (e: Throwable) {
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Sorry, but i'm having trouble connecting to the service at the moment.")
                }).awaitComplete()
            }

            if (response.status == "success") {
                val image = response.getImage()
                channel.createMessage {
                    it.addFile(response.fileName, image)
                }.await()
            } else {
                channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Sorry, but i'm having trouble connecting to the service at the moment.")
                }).await()
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
