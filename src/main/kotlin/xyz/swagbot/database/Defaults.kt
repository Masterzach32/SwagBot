package xyz.swagbot.database

import org.jetbrains.exposed.sql.select

/*
 * SwagBot - Created on 8/26/2017
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/26/2017
 */

fun getDefault(key: String): String {
    return sql {
        return@sql sb_defaults
                .select { sb_defaults.key eq key }
                .first()[sb_defaults.value]
    }
}