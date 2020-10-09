package xyz.swagbot

object EnvVars {

    private val env: Map<String, String> = System.getenv()

    val CODE_VERSION by env
    val CODE_ENV by env

    val BOT_NAME by env
    val DEFAULT_COMMAND_PREFIX by env

    val BOT_TOKEN by env

    val POSTGRES_DB by env
    val POSTGRES_USER by env
    val POSTGRES_PASSWORD by env
}
