package xyz.swagbot.features.music

fun getFormattedTime(time: Long): String {
    val hours = time / 3600
    var remainder = time % 3600
    val minutes = remainder / 60
    remainder %= 60
    val seconds = remainder

    if (hours > 0)
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    return String.format("%d:%02d", minutes, seconds)
}
