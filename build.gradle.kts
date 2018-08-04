import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.2.60"
}

allprojects {
    group = "xyz.swagbot"
    version = "2.0"

    repositories {
        mavenCentral()
        jcenter()
        maven { setUrl("https://jitpack.io") }
    }
}

application {
    mainClassName = "xyz.swagbot.AppKt"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}