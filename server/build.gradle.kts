@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotest)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktx.serialization)
    alias(libs.plugins.ktx.atomicfu)
    alias(libs.plugins.ksp)
    id(libs.plugins.zipline.get().pluginId) //TODO: 使用Zipline实现基于quickJs的接口扩展能力，结合ktor-server-di可以实现能力+性能的平衡
    id("AndroidConventionPlugin")
    id("AppRuntimePlugin")
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
                implementation(libs.micrometer.registry.prometheus)
                implementation(libs.koin.ktor)
                implementation(libs.wiregrpcserver)
                implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-ktor-server:0.10.0-grpc-122")
            }
        }
        androidMain {
            dependencies {
                implementation(projects.jvmShared)
                implementation("androidx.annotation:annotation:1.9.1")
                implementation(libs.hiddenapibypass)
                implementation(libs.koin.android)
                implementation(libs.bundles.androidx.test)
//                implementation(libs.bundles.grpc.server)
            }
        }
        androidInstrumentedTest {
            dependencies {
                implementation(libs.bundles.androidx.test)
            }
        }

        jvmMain {
            dependencies {
                implementation(projects.jvmShared)
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
            java.srcDirs("src/androidMain/java")
        }
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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
