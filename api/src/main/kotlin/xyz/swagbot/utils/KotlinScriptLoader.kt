package xyz.swagbot.utils

import java.io.File
import javax.script.ScriptEngineManager

object KotlinScriptLoader {

    val engine = ScriptEngineManager().getEngineByExtension("kts")

    inline fun <reified T> load(ktScript: File): T {
        val plugin: Any = try {
            engine.eval(ktScript.reader())
        } catch (t: Throwable) {
            throw IllegalStateException("Could not load plugin ${ktScript.name}. Caused by $t")
        }
        if (plugin is T) {
            return plugin
        } else {
            throw IllegalStateException("Could not load plugin ${ktScript.name} as ${T::class.simpleName}")
        }
    }
}