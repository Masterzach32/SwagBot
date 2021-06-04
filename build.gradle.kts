
plugins {
    kotlin("jvm") version "1.5.0"
    kotlin("plugin.serialization") version "1.5.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("com.google.cloud.tools.jib") version "3.0.0"
}

group = "xyz.swagbot"

repositories {
    mavenCentral()
    jcenter()
    maven("http://localhost:8072/artifactory/libraries/")
}

dependencies {
    implementation("com.discord4j:discord4j-core:3.1.5")
    implementation("com.sedmelluq:lavaplayer:1.3.+")
    implementation("ch.qos.logback:logback-classic:1.2.3")

    val kotlinx_coroutines_version = "1.5.+"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinx_coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$kotlinx_coroutines_version")

    val facet_version = "0.2.+"
    implementation("io.facet:facet-d4j-commands:$facet_version")
    implementation("io.facet:facet-d4j-exposed:$facet_version")
    implementation("io.facet:facet-d4j-lavaplayer-extensions:$facet_version")

    val ktor_version = "1.6.+"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-json-jvm:$ktor_version")
    implementation("io.ktor:ktor-client-serialization-jvm:$ktor_version")

    val exposed_version = "0.31.+"
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
    implementation("org.postgresql:postgresql:42.2.2")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "15"
    }

    build {
        dependsOn(jib)
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
        creationTime = "USE_CURRENT_TIMESTAMP"

        afterEvaluate {
            environment = mapOf("CODE_VERSION" to "$version")
        }
    }
}
