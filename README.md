## Overview

This library provides a simple API for evaluating Kotlin scripts from strings or files. It leverages the Kotlin Scripting API to compile and execute Kotlin scripts at runtime.

## Usage

### Gradle Dependency

Add the library to your project:

```kotlin
repositories {
    maven {
        name = "crazydev22Public"
        url = uri("https://repo.crazydev22.de/public")
    }
}

dependencies {
    implementation("de.crazydev22:kts-runner:0.0.2")
}
```

### Defining a Script Class

Before you can run scripts, you need to define a script class that will be used as the base type for your scripts. This class should be annotated with `@KotlinScript`:

```kotlin
// You'll need to import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(fileExtension = "kts")
abstract class SimpleScript
```

### Running a Script from a String

```kotlin
// Create a script runner
val scriptRunner = ScriptRunner()

// Use your defined script class

// Compile and run a script from a string
val scriptContent = """
    println("Hello from Kotlin script!")
    val result = 10 + 20
    result // The last expression is the return value
"""

val result = scriptRunner.compileText(SimpleScript::class, scriptContent).flatMap { it.evaluate() }

if (result.isSuccess()) {
    println("Script result: ${result.valueOrNull()?.getValue()}") // Outputs: Script result: 30
} else {
    println("Script failed: ${result.reports().joinToString { it.render() }}")
}
```

### Running a Script from a File

```kotlin
// Create a script runner
val scriptRunner = ScriptRunner()

// Use your defined script class

// Compile and run a script from a file
val scriptFile = File("path/to/your/script.kts")
val result = scriptRunner.compileFile(SimpleScript::class, scriptFile).flatMap { it.evaluate() }

if (result.isSuccess()) {
    println("Script result: ${result.valueOrNull()?.getValue()}")
} else {
    println("Script failed: ${result.reports().joinToString { it.render() }}")
}
```

## Building the Library

This project uses [Gradle](https://gradle.org/) with the Gradle Wrapper.

* Run `./gradlew build` to build the library
* Run `./gradlew test` to run tests
* Run `./gradlew publishToMavenLocal` to publish to your local Maven repository

## Requirements

- JDK 21 or higher
- Kotlin 2.1.20 or higher
