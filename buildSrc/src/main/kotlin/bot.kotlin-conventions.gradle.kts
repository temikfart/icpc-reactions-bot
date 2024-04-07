plugins {
    java
    kotlin("jvm")
    id("bot.common-conventions")
    id("org.jetbrains.kotlin.plugin.serialization")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

kotlin {
    compilerOptions {
        allWarningsAsErrors = true
        compilerOptions {
            freeCompilerArgs.add("-Xjvm-default=all")
//            optIn = listOf("kotlinx.serialization.ExperimentalSerializationApi")
        }
    }
}
