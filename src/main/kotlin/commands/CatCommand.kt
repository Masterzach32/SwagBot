package xyz.swagbot.commands

import io.facet.core.util.*
import io.facet.discord.commands.*
import io.facet.discord.commands.dsl.*
import io.facet.discord.commands.extensions.*
import io.facet.discord.extensions.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import xyz.swagbot.util.*

object CatCommand : ChatCommand(
    name = "Random Cat Picture",
    aliases = setOf("randomcat", "cat")
) {

    private val httpClient = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    override fun DSLCommandNode<ChatCommandSource>.register() {
        runs {
            launch { getChannel().type().await() }

            val response: Response = try {
                retry(3, 3000) {
                    httpClient.get("http://aws.random.cat/meow")
                }
            } catch (e: Throwable) {
                message.reply(errorTemplate("Sorry, but i'm having trouble getting images at the moment.", e))
                return@runs
            }

            val image = response.getImage()
            message.reply {
                file(response.fileName, image)
            }
        }
    }

    @Serializable
    private class Response(val file: String) {

        val fileName: String
            get() = file.split("/").last()

        suspend fun getImage() = retry(3, 3000) {
            httpClient.get<HttpResponse>(file).content.toInputStream()
        }
    }
}