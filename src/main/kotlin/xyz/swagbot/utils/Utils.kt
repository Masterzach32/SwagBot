package xyz.swagbot.utils

import java.util.*

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
    var list: List<String> = ArrayList(Arrays.asList(*content.split(regex.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
    list = list.map { it.trim({ it <= ' ' }) }.filter { it.isNotEmpty() }

    return list.toTypedArray()
}