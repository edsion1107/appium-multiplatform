@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.android.build.gradle.internal.tasks.ProcessJavaResTask
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ktx.serialization)
    alias(libs.plugins.ktx.atomicfu)
    alias(libs.plugins.android.convention.plugin)
    alias(libs.plugins.android.app.runtime.plugin)
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
                implementation(project.dependencies.enforcedPlatform(project(":platform")))
                implementation(libs.kotlin.logging)
                implementation(libs.ktx.datetime)
                implementation(libs.ktx.serialization.json)
                implementation(libs.bundles.ktor.server)
                implementation(libs.micrometer.registry.prometheus)
                implementation(libs.koin.ktor)
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
            }
        }
    }
    compilerOptions {
        extraWarnings.set(true)
        optIn.add("kotlin.time.ExperimentalTime")
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
    debugRuntimeOnly("io.github.smiley4:ktor-swagger-ui:5.1.0")
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

tasks.withType(ProcessJavaResTask::class.java).configureEach {
    doLast {
        val commonMainResources = outDirectory.get().asFile.resolve("../../../../../../src/commonMain/resources")
            .normalize()
        logger.info("outDirectory: ${outDirectory.asFile.orNull},commonMainResources: $commonMainResources")
        commonMainResources.copyRecursively(
            outDirectory.get().asFile,
            overwrite = true
        )
    }
}
