
val docker_username: String by project
val docker_pass: String by project
val docker_email: String by project

plugins {
    kotlin("jvm") version "1.4.10"
    id("com.google.cloud.tools.jib") version "2.6.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
}

group = "xyz.swagbot"
version = getVersionFromSemver()

repositories {
    mavenCentral()
    jcenter()
    maven("https://maven.masterzach32.net")
    maven("https://libraries.minecraft.net")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.1")
    implementation("com.sedmelluq:lavaplayer:1.3.+")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    //implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.1.0")

    val kotlinx_coroutines_version = "1.3.+"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_version")

    val facet_version = "2.0.+"
    implementation("io.facet:facet-discord4j-commands:$facet_version")
    implementation("io.facet:facet-discord4j-exposed:$facet_version")

    val ktor_version = "1.4.+"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")

    val exposed_version = "0.27.+"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:42.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }

    build {
        dependsOn(jib)
    }
}

jib {
    to {
        image = "zachkozar/swagbot:$version"
        tags = setOf("latest")

        val docker_username: String? by project
        val docker_pass: String? by project
        if (docker_username != null && docker_pass != null) {
            auth {
                username = docker_username
                password = docker_pass
            }
        }
    }

    container {
        environment = mapOf(
            "TZ" to "America/New_York",
            "CODE_VERSION" to "$version",
            "CODE_ENV" to "test",
            "BOT_NAME" to "SwagBot",
            "DEFAULT_COMMAND_PREFIX" to "~"
        )
    }
}

fun getVersionFromSemver() = file("version.properties")
    .readLines()
    .first { it.contains("version.semver") }
    .split("=")
    .last()
    .trim()
