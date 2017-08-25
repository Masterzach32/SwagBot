package xyz.swagbot.database

import org.jetbrains.exposed.sql.select

/*
 * SwagBot - Created on 8/22/17
 * Author: Zach Kozar
 * 
 * This code is licensed under the GNU GPL v3
 * You can find more info in the LICENSE file at the project root.
 */

/**
 * @author Zach Kozar
 * @version 8/22/17
 */

fun getKey(api_name: String): String {
    return sql {
        return@sql sb_api_keys
            .select { sb_api_keys.api_name eq api_name }
            .first()[sb_api_keys.api_key]
    }
}