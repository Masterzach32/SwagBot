package xyz.swagbot.utils

enum class ExitCode(val code: Int) {

    UNKNOWN(-1),
    EXITED(0),
    ERRORED(1),
    TERMINATED(130),
    RESTART_REQUESTED(32),
    UPDATE_REQUESTED(33),
    LOGIN_FAILURE(34),
    CONNECT_FAILURE(35);

    companion object {
        fun forCode(code: Int): ExitCode {
            return values().firstOrNull { it.code == code } ?: UNKNOWN
        }
    }
}