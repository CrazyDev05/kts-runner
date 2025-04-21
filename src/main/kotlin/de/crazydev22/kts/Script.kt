package de.crazydev22.kts

import de.crazydev22.kts.wrappers.Result
import de.crazydev22.kts.wrappers.EvaluationResult

interface Script {

    fun evaluate(vararg arguments: Any?): Result<EvaluationResult>
        = evaluate(emptyMap(), *arguments)

    fun evaluate(properties: Map<String, Any?>, vararg arguments: Any?): Result<EvaluationResult>
}
