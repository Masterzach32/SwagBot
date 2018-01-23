package xyz.swagbot.database

class Stat(name: String) {

    operator fun inc(): Stat {
        return this
    }
}