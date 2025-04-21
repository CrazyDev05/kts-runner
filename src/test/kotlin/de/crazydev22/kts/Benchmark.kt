package de.crazydev22.kts

import de.crazydev22.kts.scripts.FunctionScript
import org.junit.jupiter.api.Test
import java.io.File
import java.text.DecimalFormat
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.roundToInt
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Benchmark {
    private val threads = Runtime.getRuntime().availableProcessors()
    private val scriptRunner = ScriptRunner()

    companion object {
        var counter: AtomicInteger = AtomicInteger()
    }

    @Test
    fun run() {
        val tempFile = File.createTempFile("test", ".func.kts")
        tempFile.deleteOnExit()

        // Write script content to the file
        tempFile.writeText("""
            //@file:Repository("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
            //@file:DependsOn("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")

            import de.crazydev22.kts.Benchmark.Companion.counter
            import kotlin.math.pow
            import kotlin.math.sin
            import kotlin.math.sqrt

            val d = sin(x) + 2 + ((7-5) * (3.14159 * x.pow(14-10)) + sin(-3.141) + (0%x)) * x/3 * 3/sqrt(x)
            counter.getAndIncrement()
        """.trimIndent())

        val res = scriptRunner.compileFile(FunctionScript::class, tempFile)
        if (res.hasError())
            res.reports().forEach { println(it) }
        assertTrue(res.isSuccess())
        assertTrue(!res.hasError())

        val compiled = res.valueOrThrow()
        val count = 100_000

        val factory = Thread.ofPlatform().priority(10).factory()
        val tests = mapOf(
            "Fixed" to { Executors.newFixedThreadPool(threads, factory).use { testConcurrent(it, compiled, count) } },
            "Cached" to { Executors.newCachedThreadPool(factory).use { testConcurrent(it, compiled, count) } },
            "Virtual" to { testConcurrent(Executors.newVirtualThreadPerTaskExecutor(), compiled, count) },
            "Sequential" to { test(compiled, count) }
        )
        val total = count * tests.size

        val scheduler = Executors.newSingleThreadScheduledExecutor()
        val task = scheduler.scheduleAtFixedRate({
            val completed = counter.get()
            println("[PROGRESS] ${(completed / total.toDouble()).times(10000).roundToInt().div(100.0)}% ${completed}/$total")
        }, 1000, 5000, TimeUnit.MILLISECONDS)

        val result = tests.map { it.value.invoke().result(it.key) }
        task.cancel(true)
        assertEquals(counter.get(), total)
        result.print()
    }

    private fun testConcurrent(
        service: ExecutorService,
        script: Script,
        count: Int
    ): Stats {
        val stats = Stats()
        val latch = CountDownLatch(count)
        for (i in 1..count) {
            service.submit {
                try {
                    stats.add(invoke(script))
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        return stats
    }

    private fun test(script: Script, count: Int): Stats {
        val stats = Stats()
        for (i in 1..count) {
            stats.add(invoke(script))
        }
        return stats
    }

    @Suppress("UNCHECKED_CAST")
    private fun Script.dump() {
        File("dump").mkdirs()
        KJvmCompiledScript::class.java.getDeclaredField("compiledModule").apply { isAccessible = true }
            .get(CachedScript::class.java.getDeclaredField("base").apply { isAccessible = true }
                .get(this))
            .let { it.javaClass.getDeclaredField("compilerOutputFiles").apply { isAccessible = true }.get(it) as Map<String, ByteArray> }
            .forEach { File(File("dump"),it.key.replace("/", "__")).writeBytes(it.value) }
    }

    private fun invoke(script: Script): Double {
        val x = Math.random() * Int.Companion.MAX_VALUE
        val start = Instant.now()
        val r2 = script.evaluate(x)
        val time = Duration.between(start, Instant.now()).toNanos() / 1000000.0
        r2.reports().forEach(System.out::println)
        return time
    }

    private class Stats {
        val min = AtomicReference<Double>()
        var max = AtomicReference<Double>()
        val total = AtomicReference<Double>()
        val count = AtomicLong()

        fun add(value: Double) {
            min.updateAndGet { v -> v?.let { minOf(v, value) } ?: value }
            max.updateAndGet { v -> v?.let { maxOf(v, value) } ?: value }
            total.updateAndGet { v -> v?.let { v + value } ?: value }
            count.incrementAndGet()
        }

        fun result(name: String): Result<String, Double, Double, Double> {
            val min = min.get()!!
            val max = max.get()!!
            val count = count.get()
            val avg = total.get() / count
            return Result(name, avg, min, max)
        }
    }

    private data class Result<A,B,C,D>(val a: A, val b: B, val c: C, val d: D)

    private fun Result<String, Double, Double, Double>.format() =
        Result(a, b.trim(), c.trim(), d.trim())

    private fun Result<String, String, String, String>.toLine(
        aL: Int, bL: Int, cL: Int, dL: Int, separator: String = " | "
    ) = listOf(a.padEnd(aL), b.padEnd(bL), c.padEnd(cL), d.padEnd(dL)).joinToString(separator)

    private fun List<Result<String, Double, Double, Double>>.print() {
        val lines = map { it.format() }.toMutableList()
        lines.add(0, Result("Name", "Average", "Min", "Max"))
        val a = lines.maxOf { it.a.length } + 1
        val b = lines.maxOf { it.b.length } + 1
        val c = lines.maxOf { it.c.length } + 1
        val d = lines.maxOf { it.d.length } + 1
        lines.add(1, Result(
            "-".repeat(a),
            "-".repeat(b),
            "-".repeat(c),
            "-".repeat(d)
        ))

        lines.forEachIndexed { index, result ->
            println("[RESULT] " + result.toLine(a, b, c, d, if (index == 1) "-|-" else " | "))
        }
    }

    private val format = DecimalFormat("#.######")
    private fun Double.trim(): String {
        return format.format(this)
    }
}
