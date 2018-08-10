import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
}

group = "xyz.swagbot"
version = "2.0"
val mainClass = "xyz.swagbot.AppKt"

application {
    mainClassName = mainClass
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile(kotlin("compiler-embeddable"))
    compile(kotlin("script-runtime"))
    compile(kotlin("script-util"))
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

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = mainClass
    }

    configurations["compileClasspath"].forEach { from(zipTree(it.absoluteFile)) }
}

tasks.withType<JavaExec> {
    args = listOf("SQLITE", "storage/storage.db")
    this.workingDir = file("$rootDir/run/")
}

tasks.getByName("jar").doLast {
    file("$buildDir/libs").walkTopDown().first { it.isFile }.copyTo(file("$rootDir/run/SwagBot.jar"), true)
}