import com.bmuschko.gradle.docker.tasks.image.*

val docker_username: String by project
val docker_pass: String by project
val docker_email: String by project

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.bmuschko.docker-java-application") version "6.6.1"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
}

group = "xyz.swagbot"
version = getVersionFromSemver()

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.1")
    implementation("com.sedmelluq:lavaplayer:1.3.50")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    val facet_version = "2.0.+"
    implementation("io.facet:discord-commands:$facet_version")
    implementation("io.facet:exposed-discord:$facet_version")

    val kotlinx_coroutines_version = "1.3.9"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_version")

    val ktor_version = "1.4.1"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")

    val exposed_version = "0.27.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:42.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    incrementPatch {
        dependsOn(classes)
        onlyIf { compileKotlin.get().didWork }
    }

    dockerSyncBuildContext {
        dependsOn(classes)
    }

    val createDockerfile by registering(Dockerfile::class) {
        group = "swagbot"
        dependsOn(incrementPatch, dockerSyncBuildContext)

        from("openjdk:jre-alpine")

        label(mapOf("maintainer" to "Zachary Kozar 'zachkozar@vt.edu'"))

        environmentVariable(
            mapOf(
                "TZ" to "America/New_York",
                "CODE_VERSION" to "$version",
                "CODE_ENV" to "test",
                "BOT_NAME" to "SwagBot",
                "DEFAULT_COMMAND_PREFIX" to "~"
            )
        )

        workingDir("/app")

        copyFile("libs", "libs/")
        copyFile("resources", "resources/")
        copyFile("classes", "classes/")

        entryPoint(
            "java",
            "-cp",
            "/app/resources:/app/classes:/app/libs/*",
            "xyz.swagbot.SwagBot"
        )
    }

    val buildImage by registering(DockerBuildImage::class) {
        group = "swagbot"
        dependsOn(createDockerfile)
        images.set(setOf("zachkozar/swagbot:$version", "zachkozar/swagbot:latest"))
    }

    build {
        dependsOn(buildImage)
    }
}

fun getVersionFromSemver() = file("version.properties")
    .readLines()
    .first { it.contains("version.semver") }
    .split("=")
    .last()
    .trim()
