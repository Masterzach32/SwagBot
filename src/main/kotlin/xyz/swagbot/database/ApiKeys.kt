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
        val key = ApiKeys.select { ApiKeys.api_name eq api_name }.firstOrNull()
        if (key != null)
            return@sql key[ApiKeys.api_key]
        else
            throw MissingApiKeyException(api_name)
    }
}

class MissingApiKeyException(api: String) :
        Throwable("Missing api key from database: $api. Have you set up the bot's api token in the sql database?")