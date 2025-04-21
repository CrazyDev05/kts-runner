package de.crazydev22.kts.wrappers

import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.isError
import kotlin.script.experimental.api.valueOrNull
import kotlin.script.experimental.api.valueOrThrow

class Result<T>(direct: ResultWithDiagnostics<T>) : JvmWrapper<ResultWithDiagnostics<T>>(direct) {

    fun valueOrNull() = direct().valueOrNull()
    fun valueOrThrow() = direct().valueOrThrow()
    fun reports() = direct().reports.map(::Diagnostic)

    fun isSuccess() = direct() is ResultWithDiagnostics.Success
    fun isError() = direct() is ResultWithDiagnostics.Failure
    fun hasError() = direct().reports.any { it.isError() }

    fun <R> map(transform: (T) -> R): Result<R> = with(direct()) {
        when (this) {
            is ResultWithDiagnostics.Failure -> Result(this)
            is ResultWithDiagnostics.Success -> Result(ResultWithDiagnostics.Success(transform(value), reports))
        }
    }

    fun <R> flatMap(transform: (T) -> Result<R>): Result<R> = with(direct()) {
        when (this) {
            is ResultWithDiagnostics.Failure -> Result(this)
            is ResultWithDiagnostics.Success -> transform(value)
        }
    }
}
