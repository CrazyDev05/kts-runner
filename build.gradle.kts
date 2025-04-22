plugins {
    alias(libs.plugins.kotlinJvm)
    `java-library`
    `maven-publish`
}

version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library
    api(libs.kotlin.stdlib)

    // Kotlin coroutines dependencies
    api(libs.kotlin.coroutines)

    // Kotlin scripting dependencies
    api(libs.kotlin.scripting.common)
    api(libs.kotlin.scripting.jvm)
    api(libs.kotlin.scripting.jvm.host)
    api(libs.kotlin.scripting.dependencies.maven)

    // Test dependencies
    testImplementation(libs.kotlin.test)
    testImplementation(libs.junit.jupiter)
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("library") {
            from(components["java"])

            groupId = "de.crazydev22"
            artifactId = "kts-runner"
            version = "0.1.0"
        }

        repositories {
            maven {
                name = "CrazyDev22"
                url = uri("https://repo.crazydev22.de/public")
                credentials(PasswordCredentials::class)
            }
        }
    }
}
