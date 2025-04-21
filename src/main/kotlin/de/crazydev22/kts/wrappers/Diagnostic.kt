package de.crazydev22.kts.wrappers

import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.isError

class Diagnostic(direct: ScriptDiagnostic) : JvmWrapper<ScriptDiagnostic>(direct) {

    /**
     * Render diagnostics message as a string in a form:
     * "[SEVERITY ]message[ (file:line:column)][: exception message[\n exception stacktrace]]"
     * @param withSeverity add severity prefix, true by default
     * @param withLocation add error location in the compiled script, if present, true by default
     * @param withException add exception message, if present, true by default
     * @param withStackTrace add exception stacktrace, if exception is present and [withException] is true, false by default
     */
    @JvmOverloads
    fun render(
        withSeverity: Boolean = true,
        withLocation: Boolean = true,
        withException: Boolean = true,
        withStackTrace: Boolean = false
    ): String = direct().render(withSeverity, withLocation, withException, withStackTrace)

    fun isError() = direct().isError()
}
