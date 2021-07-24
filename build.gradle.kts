
plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.0"
    id("com.google.cloud.tools.jib") version "3.1.2"
    id("net.researchgate.release") version "2.8.1"
}

group = "xyz.swagbot"
val isRelease = !version.toString().endsWith("-SNAPSHOT")

repositories {
    mavenCentral()
    maven("https://maven.masterzach32.net/artifactory/libraries")
    maven("https://m2.dv8tion.net/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation("com.sedmelluq:lavaplayer:1.3.+")
    implementation("com.sedmelluq:lavaplayer-natives-extra:1.3.+")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    val facet_version = "0.3.0-SNAPSHOT"
    implementation("io.facet:facet-d4j-commands:$facet_version")
    implementation("io.facet:facet-d4j-application-commands:$facet_version")
    implementation("io.facet:facet-d4j-exposed:$facet_version")
    implementation("io.facet:facet-d4j-lavaplayer-extensions:$facet_version")

    val ktor_version = "1.6.+"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")

    val exposed_version = "0.32.+"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:42.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "16"
            freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
        }
    }
}


jib {
    from {
        image = "openjdk:16"

        platforms {
            platform {
                architecture = "amd64"
                os = "linux"
            }
            platform {
                architecture = "arm64"
                os = "linux"
            }
        }
    }

    to {
        image = "zachkozar/swagbot:$version"

        val imageTags = version.toString()
            .substringBefore("-")
            .split(".")
            .fold(mutableSetOf<String>()) { tags, subVersion ->
                tags.apply {
                    if (tags.isEmpty())
                        tags.add(subVersion)
                    else
                        tags.add("${tags.last()}.$subVersion")
                }
            }
            .map { if (!isRelease) "$it-SNAPSHOT" else it }
            .toMutableSet()

        if (isRelease)
            imageTags.add("prod")
        imageTags.add("latest")
        tags = imageTags

        val dockerUsername: String? by project
        val dockerPassword: String? by project
        if (dockerUsername != null && dockerPassword != null) {
            auth {
                username = dockerUsername
                password = dockerPassword
            }
        }
    }

    container {
        creationTime = "USE_CURRENT_TIMESTAMP"

        environment = mapOf(
            "TZ" to "America/New_York",
            "CODE_VERSION" to "$version",
            "CODE_ENV" to "test",
            "BOT_NAME" to "SwagBot",
            "DEFAULT_COMMAND_PREFIX" to "~"
        )
    }
}
