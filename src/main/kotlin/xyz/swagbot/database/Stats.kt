package xyz.swagbot.database

object Stats {

    val map: Map<String, () -> Any>

    init {
        map = mapOf(
                Pair("command_count", { 0 })
        )
    }
}