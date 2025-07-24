import com.android.build.gradle.internal.tasks.ProcessJavaResTask

plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.android.convention.plugin)
    alias(libs.plugins.android.app.runtime.plugin)
}

group = "io.appium.multiplatform"
version = "unspecified"


kotlin {
    androidTarget()
//    iosArm64()
    jvm()
    sourceSets {
        commonMain {
            dependencies {
                implementation(project.dependencies.enforcedPlatform(project(":platform")))
                implementation(libs.kotlin.logging)
                implementation(libs.bundles.ktor.server)
                implementation(libs.koin.ktor)
            }
        }
        commonTest {
            dependencies {

            }
        }

        androidMain {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("androidx.annotation:annotation:1.9.1")
                implementation(libs.hiddenapibypass)
                implementation(libs.slf4j.simple)
            }
        }
        androidInstrumentedTest {
            dependencies {
                implementation(libs.bundles.androidx.test)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.adblib)
                implementation(libs.slf4j.simple)
                implementation("com.sksamuel.cohort:cohort-ktor:2.7.2") // jvm only
            }
        }
    }
}

android {
    namespace = "com.appium.multiplatform"
    sourceSets {
        getByName("main") {
            java.srcDirs("src/androidMain/java", "src/androidMain/aidl", "src/sharedJvmAndroid/java")
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
    mainClass = "io.appium.multiplatform.MainKt"
//    isolation = true
    vmOptions.set(
        mapOf(
            "kotlin-logging-to-android-native" to "true",
            "org.slf4j.simpleLogger.logFile" to "System.out"
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