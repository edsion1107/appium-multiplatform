@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi


plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.ktx.serialization)
}

group = "io.appium.multiplatform"
version = "unspecified"

/**
 * Kotlin doesn't currently support sharing a source set for JVM + Android targets
 */
kotlin {
    jvm() // at least one Kotlin target
    compilerOptions {
        extraWarnings.set(true)
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(projects.platform))
                api(kotlin("reflect"))
                api(libs.bundles.ktor.shared)
                api(libs.kotlin.logging)
                api(libs.ktx.datetime)
                api(libs.ktx.serialization.json)
                api(libs.cache4k)
                implementation(libs.wire.runtime) // 暂时先不用wire
            }
        }
    }
}
