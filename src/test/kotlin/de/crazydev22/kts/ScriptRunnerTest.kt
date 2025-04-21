package de.crazydev22.kts

import de.crazydev22.kts.scripts.SimpleScript
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

class ScriptRunnerTest {

    private val scriptRunner = ScriptRunner()

    @Test
    fun `test evaluating a simple script from string`() {
        val script = """
            val x = 10
            val y = 20
            x + y
        """.trimIndent()

        val result = scriptRunner.compileText(SimpleScript::class, script).flatMap { it.evaluate() }

        assertTrue(result.isSuccess())
        assertEquals(30, result.valueOrNull()?.getValue())
        assertTrue(!result.hasError())
    }

    @Test
    fun `test evaluating a script with error`() {
        val script = """
            val x = 10
            x.nonExistentMethod() // This will cause an error
        """.trimIndent()

        val result = scriptRunner.compileText(SimpleScript::class, script).flatMap { it.evaluate() }

        assertTrue(result.isError())
        assertNull(result.valueOrNull()?.getValue())
        assertTrue(result.hasError())
    }

    @Test
    fun `test evaluating a script with println`() {
        val script = """
            println("Hello, World!")
            42
        """.trimIndent()

        val result = scriptRunner.compileText(SimpleScript::class, script).flatMap { it.evaluate() }

        assertTrue(result.isSuccess())
        assertEquals(42, result.valueOrNull()?.getValue())
        assertTrue(!result.hasError())
    }

    @Test
    fun `test evaluating a script from file`() {
        val tempFile = File.createTempFile("test", ".kts")
        tempFile.deleteOnExit()

        tempFile.writeText("""
            val a = 100
            val b = 200
            a * b
        """.trimIndent())

        val result = scriptRunner.compileFile(SimpleScript::class, tempFile).flatMap { it.evaluate() }

        assertTrue(result.isSuccess())
        assertEquals(20000, result.valueOrNull()?.getValue())
        assertTrue(!result.hasError())
    }

    @Test
    fun `test script caching optimization`() {
        val complexScript = """
            // A more complex script with multiple functions and calculations
            fun factorial(n: Int): Int {
                return if (n <= 1) 1 else n * factorial(n - 1)
            }
            
            fun fibonacci(n: Int): Int {
                return when (n) {
                    0, 1 -> n
                    else -> fibonacci(n - 1) + fibonacci(n - 2)
                }
            }
            
            val factResult = factorial(10)
            val fibResult = fibonacci(10)
            factResult + fibResult
        """.trimIndent()

        val startTime0 = System.currentTimeMillis()
        val compile = scriptRunner.compileText(SimpleScript::class, complexScript)
        val duration0 = System.currentTimeMillis() - startTime0

        val startTime1 = System.currentTimeMillis()
        val result1 = compile.flatMap { it.evaluate() }
        val duration1 = System.currentTimeMillis() - startTime1

        val startTime2 = System.currentTimeMillis()
        val result2 = compile.flatMap { it.evaluate() }
        val duration2 = System.currentTimeMillis() - startTime2

        assertTrue(!result1.isError())
        assertTrue(!result2.isError())
        assertEquals(result1.valueOrNull()?.getValue(), result2.valueOrNull()?.getValue())

        println("[DEBUG_LOG] Compilation duration: $duration0 ms")
        println("[DEBUG_LOG] First execution duration: $duration1 ms")
        println("[DEBUG_LOG] Second execution duration: $duration2 ms")
    }
}