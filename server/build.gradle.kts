@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktx.serialization)
    alias(libs.plugins.ktx.atomicfu)
    id(libs.plugins.zipline.get().pluginId) // TODO: Use Zipline to implement interface extension capabilities based on QuickJS
    alias(libs.plugins.convention.android)
    alias(libs.plugins.convention.appruntime)
    alias(libs.plugins.convention.kotest)
}

group = "io.appium.multiplatform"
version = "unspecified"


kotlin {
    androidTarget()
    jvm {
        mainRun {
            mainClass.set("io.appium.multiplatform.server.ApplicationKt")
        }
    }
    sourceSets {
        commonMain {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(projects.platform))
                implementation(projects.shared)
                implementation(libs.bundles.ktor.server)
//                implementation(libs.micrometer.registry.prometheus)
                implementation(libs.koin.ktor)
            }
        }
        commonTest {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(projects.platform))
                implementation(libs.kotest.framework.engine)
                implementation(libs.kotest.runner.junit5)
                implementation(libs.ktor.server.test.host)
                implementation(libs.bundles.ktor.client)
                implementation(libs.kotest.extensions.koin)
                implementation(libs.kotest.assertions.ktor)
                implementation(libs.kotest.extensions.allure)
                implementation(libs.mockk)
            }
        }
        androidMain {
            kotlin.srcDir("src/jvmBase/kotlin")
            dependencies {
                // Since this includes a KMP submodule with a shared target (which doesn’t support Android),
                // it effectively forces a dependency on the submodule’s JVM target code.
                implementation(projects.shared)
//                implementation("androidx.annotation:annotation:1.9.1")
                implementation(libs.hiddenapibypass)
                implementation(libs.koin.android)
                implementation(libs.bundles.androidx.test)
            }
        }
        androidUnitTest{
            dependencies {
                implementation(libs.mockk.android)
                implementation(libs.mockk.agent)
            }
        }

        jvmMain {
            kotlin.srcDir("src/jvmBase/kotlin")
            dependencies {
                implementation(libs.adblib)
                implementation(libs.koin.logger.slf4j)
            }
        }
    }
    compilerOptions {
        extraWarnings.set(true)   // TODO: wire plugin not support, waiting for a fix
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

android {
    namespace = "com.appium.multiplatform.server"
    sourceSets {
        getByName("main") {
            // java and resources not work in kotlin.sourceSets.androidMain
            java.srcDirs("src/androidMain/java")
            resources.srcDir("src/commonMain/resources")
        }
    }
    testOptions{
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}


appRuntime {
//    mainClass = "io.appium.multiplatform.server.DemoKt"
    mainClass = "io.appium.multiplatform.server.ApplicationKt"
//    isolation = true
    vmOptions.set(
        mapOf(
//            "kotlin-logging-to-android-native" to "true",
//            "org.slf4j.simpleLogger.logFile" to "System.out"
//            "io.ktor.development" to "true", // Auto-reload not support for android
        )
    )
//    args.set("sleep=true")
}
