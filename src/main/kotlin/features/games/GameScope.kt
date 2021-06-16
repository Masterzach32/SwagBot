package xyz.swagbot.features.games

import kotlinx.coroutines.*
import kotlin.coroutines.*

class GameScope(context: CoroutineContext) : CoroutineScope by CoroutineScope(context)