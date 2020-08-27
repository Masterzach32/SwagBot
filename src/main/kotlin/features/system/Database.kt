package xyz.swagbot.features.system

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*

fun <T> sql(sqlcode: Transaction.() -> T): T = transaction(statement = sqlcode)