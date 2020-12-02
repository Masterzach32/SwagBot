package xyz.swagbot.features.system

import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*

suspend fun <T> sql(sqlcode: Transaction.() -> T): T = withContext(Dispatchers.IO) {
    transaction(statement = sqlcode)
}