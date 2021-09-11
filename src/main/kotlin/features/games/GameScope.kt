package xyz.swagbot.features.games

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

class GameScope(context: CoroutineContext) : CoroutineScope by CoroutineScope(context)