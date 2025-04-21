package de.crazydev22.kts.wrappers

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue

class EvaluationResult(direct: EvaluationResult) : JvmWrapper<EvaluationResult>(direct) {
    fun getScriptClass() = direct().returnValue.scriptClass
    fun getScriptInstance() = direct().returnValue.scriptInstance

    fun getName() = with(direct().returnValue) {
        if (this !is ResultValue.Value) null
        else name
    }

    fun getValue() = with(direct().returnValue) {
        if (this !is ResultValue.Value) null
        else value
    }

    fun getError() = with(direct().returnValue) {
        if (this !is ResultValue.Error) null
        else error
    }

    fun getWrappingException() = with(direct().returnValue) {
        if (this !is ResultValue.Error) null
        else wrappingException
    }

    fun getType() = when (direct().returnValue) {
        is ResultValue.Value -> Type.VALUE
        is ResultValue.Unit -> Type.UNIT
        is ResultValue.Error -> Type.ERROR
        is ResultValue.NotEvaluated -> Type.NOT_EVALUATED
    }

    enum class Type { VALUE, UNIT, ERROR, NOT_EVALUATED }
}
