import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

group = "xyz.swagbot"
version = "2.0"

repositories {
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("script-runtime"))
    compile(project(":api"))

    compile("ch.qos.logback:logback-classic:1.2.3")

    compile("com.discord4j:Discord4J:2.10.1")
    compile("com.github.natanbc:discordbots-api:1.4")
    compile("com.sedmelluq:lavaplayer:1.2.56")

    compile("org.xerial:sqlite-jdbc:3.20.0")
    compile("mysql:mysql-connector-java:8.0.11")
    compile("com.github.JetBrains:Exposed:0.10.4")

    compile("com.mashape.unirest:unirest-java:1.4.9")
    compile("org.jsoup:jsoup:1.11.2")
    compile("se.michaelthelin.spotify:spotify-web-api-java:2.0.3")

    compile("com.github.masterzach32:VT-Timetable-Api:1.0.1")
    compile("com.github.masterzach32:Commands4K:1.2.2")
    compile("com.vdurmont:emoji-java:4.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.create("copyPlugins") {
    doLast {
        val runDir = rootDir.walkTopDown().first { it.isDirectory && it.name == "run" }
        var pluginsDir = runDir.walkTopDown().firstOrNull { it.isDirectory && it.name == "plugins" }

        if (pluginsDir == null) {
            pluginsDir = file("${runDir.absolutePath}/plugins/")
        } else {
            pluginsDir.deleteRecursively()
        }

        pluginsDir.mkdir()

        file("$projectDir/src/main/kotlin").walkTopDown().asSequence()
                .filter { it.isFile }
                .forEach { it.copyTo(file("$pluginsDir/${it.name}")) }
    }
}
