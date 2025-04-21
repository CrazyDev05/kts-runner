package de.crazydev22.kts.scripts

import kotlin.script.experimental.annotations.KotlinScript

/**
 * A more complex Kotlin script definition.
 * This class defines the base script type used by the ScriptRunner.
 */
@KotlinScript(fileExtension = "func.kts")
abstract class FunctionScript(val x: Double)