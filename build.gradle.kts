import com.bmuschko.gradle.docker.tasks.image.*
import com.bmuschko.gradle.docker.tasks.container.*

val logback_version: String by project
val exposed_version: String by project
val mysql_connector_version: String by project

val discord4j_version: String by project
val facet_version: String by project
val lavaplayer_version: String by project

val docker_username: String by project
val docker_pass: String by project
val docker_email: String by project

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.bmuschko.docker-java-application") version "6.1.3"
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
    implementation(kotlin("stdlib-jdk8"))

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("mysql:mysql-connector-java:$mysql_connector_version")
    implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
    implementation("org.jetbrains.exposed:exposed-jodatime:$exposed_version")

    implementation("com.discord4j:discord4j-core:$discord4j_version")
    implementation("io.facet:commands:$facet_version")
    implementation("com.sedmelluq:lavaplayer:$lavaplayer_version")
}

docker {
    registryCredentials {
        username.set(docker_username)
        password.set(docker_pass)
        email.set(docker_email)
    }
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
}

val createSwagBotDockerfile by tasks.registering(Dockerfile::class) {
    group = "docker"
    dependsOn(tasks.getByPath("build"), tasks.getByPath("dockerSyncBuildContext"))

    from("openjdk:8-jre")

    label(mapOf("maintainer" to "Zachary Kozar 'zachk@vt.edu'"))

    mapOf(
        "BOT_TOKEN" to properties["swagbeta_token"],
        "DB_USERNAME" to properties["swagbot_db_username"],
        "DB_PASS" to properties["swagbot_db_pass"]
    ).mapValues { (_, value) -> value.toString() }.let { environmentVariable(it) }

    environmentVariable("TZ", "America/New_York")

    workingDir("/app")

    copyFile("libs", "libs/")
    copyFile("resources", "resources/")
    copyFile("classes", "classes/")

    entryPoint(
        "java",
        "-Xms256m",
        "-Xmx2G",
        "-cp",
        "/app/resources:/app/classes:/app/libs/*",
        "xyz.swagbot.SwagBot"
    )
}

val buildSwagBotImage by tasks.registering(DockerBuildImage::class) {
    group = "docker"
    dependsOn(createSwagBotDockerfile)
    images.set(setOf("zachkozar/swagbot:$version", "zachkozar/swagbot:latest"))
}

val stopSwagBotContainer by tasks.registering(DockerStopContainer::class) {
    group = "docker"
    targetContainerId("swagbot")
    onError {
        logger.lifecycle("No container to stop, skipping.")
    }
}

val removeSwagBotContainer by tasks.registering(DockerRemoveContainer::class) {
    group = "docker"
    dependsOn(stopSwagBotContainer)
    targetContainerId("swagbot")
    onError {
        if (this.message?.contains("No such container") != true)
            throw this
        else
            logger.lifecycle("No container to remove, skipping.")
    }
}

val createSwagBotContainer by tasks.registering(DockerCreateContainer::class) {
    group = "docker"
    dependsOn(buildSwagBotImage, removeSwagBotContainer)
    targetImageId(buildSwagBotImage.get().imageId)
    hostConfig.autoRemove.set(false)
    hostConfig.restartPolicy.set("always")
    hostConfig.links.set(listOf("mysql:db"))
    containerName.set("swagbot")
}

val startSwagBotContainer by tasks.registering(DockerStartContainer::class) {
    group = "docker"
    dependsOn(createSwagBotContainer)
    targetContainerId(createSwagBotContainer.get().containerId)
}

val pushSwagBotImage by tasks.registering(DockerPushImage::class) {
    group = "docker"
    dependsOn(buildSwagBotImage)
    images.set(setOf("zachkozar/swagbot:$version", "zachkozar/swagbot:latest"))
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get())
}

fun getVersionFromSemver() = file("version.properties")
    .readLines()
    .first { it.contains("version.semver") }
    .split("=")
    .last()
    .trim()
