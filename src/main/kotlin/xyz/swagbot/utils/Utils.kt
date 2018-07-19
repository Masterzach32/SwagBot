package xyz.swagbot.utils

import com.vdurmont.emoji.EmojiParser
import net.masterzach32.commands4k.AdvancedMessageBuilder
import sx.blah.discord.handle.impl.obj.ReactionEmoji
import sx.blah.discord.handle.obj.IGuild
import java.net.URL
import java.text.SimpleDateFormat

/*
 * SwagBot - Created on 9/1/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 9/1/2017
 */

/**
 * Concatenate a string from an array.
 *
 * @param args
 * @param start
 * @param end
 * @return
 */
fun getContent(args: Array<String>, start: Int, end: Int): String {
    var content = ""
    for (i in start until Math.min(end, args.size)) {
        content += args[i]
        if (i != Math.min(end, args.size) - 1) {
            content += " "
        }
    }

    return content
}

/**
 * Concatenate a string from an array.
 *
 * @param args
 * @param start
 * @return
 */
fun getContent(args: Array<String>, start: Int): String {
    return getContent(args, start, args.size)
}

/**
 * Delimits the string by the regex, trimming and removing tokens that are null or empty.
 *
 * @param content
 * @param regex
 * @return
 */
fun delimitWithoutEmpty(content: String, regex: String): Array<String> {
    return content.split(regex.toRegex())
            .dropLastWhile { it.isEmpty() }
            .map { it.trim { it <= ' ' } }
            .filter { it.isNotEmpty() }
            .toTypedArray()
}

fun AdvancedMessageBuilder.withImage(url: String): AdvancedMessageBuilder {
    return withFile(URL(url).openStream(), url) as AdvancedMessageBuilder
}

fun Thread(name: String, func: () -> Unit): Thread {
    return Thread(func, name)
}

fun getFormattedTime(time: Int): String {
    val hours = time / 3600
    var remainder = time % 3600
    val minutes = remainder / 60
    remainder %= 60
    val seconds = remainder

    if (hours > 0)
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    return String.format("%d:%02d", minutes, seconds)
}

fun listOfEmojis(vararg emojis: String) = emojis.map { ReactionEmoji.of(EmojiParser.parseToUnicode(":$it:")) }

inline fun <reified E> List<E>.split(newSize: Int): List<List<E>> {
    val lists = arrayOfNulls<MutableList<E>?>(size / newSize + 1)
    for (i in indices) {
        when {
            i % size == 0 -> lists[i / size] = mutableListOf(get(i))
            i % size > 0 -> lists[i / size]!!.add(get(i))
        }
    }
    return lists.filterNotNull().filter { it.isNotEmpty() }
}