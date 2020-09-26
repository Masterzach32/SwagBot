package xyz.swagbot.commands

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

object CatCommand : ChatCommand(
    name = "Random Cat Picture",
    aliases = setOf("randomcat", "cat")
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
                    httpClient.get("http://aws.random.cat/meow")
                }
            } catch (e: Throwable) {
                return@runs channel.createEmbed(errorTemplate.andThen {
                    it.setDescription("Sorry, but i'm having trouble connecting to the service at the moment.")
                }).awaitComplete()
            }

            val image = response.getImage()
            channel.createMessage {
                it.addFile(response.fileName, image)
            }.awaitComplete()
        }
    }

    private class Response(val file: String) {

        val fileName: String
            get() = file.split("/").last()

        suspend fun getImage() = retry(3, 3000) {
            httpClient.get<HttpResponse>(file).content.toInputStream()
        }
    }
}
