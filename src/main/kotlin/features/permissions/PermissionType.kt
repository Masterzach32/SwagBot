package xyz.swagbot.features.permissions

enum class PermissionType(val codeName: String) {
    NONE("none"),
    NORMAL("normal"),
    MOD("moderator"),
    ADMIN("admin"),
    DEV("developer");

    companion object {

        fun all() = values()

        fun allAvailable() = values().filterNot { it == DEV }
    }
}
