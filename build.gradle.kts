
plugins {
    kotlin("jvm") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("com.google.cloud.tools.jib") version "3.1.1"
}

group = "xyz.swagbot"

repositories {
    mavenCentral()
    mavenLocal()
    jcenter()
    //maven("https://maven.masterzach32.net")
    maven("https://libraries.minecraft.net")
    maven("https://m2.dv8tion.net/releases")

    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.spring.io/milestone")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.2.0-SNAPSHOT")
    implementation("com.sedmelluq:lavaplayer:1.3.+")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    val kotlinx_coroutines_version = "1.5.+"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_version")

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
        kotlinOptions.jvmTarget = "16"
    }

    incrementBuildMeta {
        doFirst {
            buildMeta = (buildMeta.toInt() + 1).toString()
        }
    }
}


jib {
    from {
        image = "openjdk:16"
    }

    to {
        afterEvaluate {
            image = "zachkozar/swagbot:$version"
            tags = setOf(
                "latest",
                "${semver.major}",
                "${semver.major}.${semver.minor}",
                "${semver.major}.${semver.minor}.${semver.patch}"
            )
        }

        val dockerUsername = findProperty("docker_username")?.toString()
        val dockerPassword = findProperty("docker_pass")?.toString()
        if (dockerUsername != null && dockerPassword != null) {
            auth {
                username = dockerUsername
                password = dockerPassword
            }
        }
    }

    container {
        creationTime = "USE_CURRENT_TIMESTAMP"

        afterEvaluate {
            environment = mapOf(
                "TZ" to "America/New_York",
                "CODE_VERSION" to "$version",
                "CODE_ENV" to "test",
                "BOT_NAME" to "SwagBot",
                "DEFAULT_COMMAND_PREFIX" to "~"
            )
        }
    }
}
