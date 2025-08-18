@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktx.serialization)
    alias(libs.plugins.ktx.atomicfu)
    id(libs.plugins.zipline.get().pluginId) //TODO: 使用Zipline实现基于quickJs的接口扩展能力，结合ktor-server-di可以实现能力+性能的平衡
    id("AndroidConventionPlugin")
    id("AppRuntimePlugin")
}

group = "io.appium.multiplatform"
version = "unspecified"


kotlin {
    androidTarget()
//    iosArm64()
    jvm{
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
                implementation("org.jetbrains.kotlinx:kotlinx-rpc-grpc-ktor-server:0.10.0-grpc-122")
            }
        }
        commonTest {
            dependencies {

            }
        }

        androidMain {
            dependencies {
                implementation(projects.jvmShared)
                implementation("androidx.annotation:annotation:1.9.1")
                implementation(libs.hiddenapibypass)
                implementation(libs.koin.android)
                implementation(libs.bundles.androidx.test)
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
//        extraWarnings.set(true)   // TODO: wire plugin not support, waiting for a fix
        optIn.add("kotlin.time.ExperimentalTime")
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

// dependencies for variant, make output smaller
dependencies {
    //TODO: add ktor plugins
//    debugRuntimeOnly("io.github.smiley4:ktor-swagger-ui:5.1.0")
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
