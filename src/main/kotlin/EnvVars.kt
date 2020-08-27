package xyz.swagbot

object EnvVars {

    private val env = System.getenv()

    val CODE_VERSION: String by env
    val CODE_ENV: String by env

    val BOT_NAME: String by env
    val DEFAULT_COMMAND_PREFIX: String by env

    val BOT_TOKEN: String by env

    val POSTGRES_DB: String by env
    val POSTGRES_USER: String by env
    val POSTGRES_PASSWORD: String by env
}
