package xyz.swagbot.features.bgw

import io.facet.exposed.snowflake
import org.jetbrains.exposed.sql.Table

object ListTable : Table("bgw_list") {

    val user_id = snowflake("user_id")
    val minTimeout = integer("min_timeout").default(1 * 3600 * 1000)
    val maxTimeout = integer("max_timeout").default(4 * 3600 * 1000)
}
