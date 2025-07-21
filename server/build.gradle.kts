buildscript {
    dependencies {
        classpath(libs.adblib)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.android.convention.plugin)
    alias(libs.plugins.android.app.runtime.plugin)
    alias(libs.plugins.project.report)
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
                implementation("com.sksamuel.hoplite:hoplite-ktor:1.2.3")
                runtimeOnly("io.github.smiley4:ktor-swagger-ui:5.1.0")
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
                implementation("com.google.testing.platform:core:0.0.9-alpha03")
                implementation("com.sksamuel.cohort:cohort-ktor:2.7.2") // jvm only
            }
        }
    }
}

android {
    namespace = "com.appium.multiplatform"
    sourceSets {
        getByName("main") {
            java.srcDirs("src/androidMain/java", "src/androidMain/aidl")
        }
    }
}

appRuntime {
    mainClass = "io.appium.multiplatform.server.DemoKt"
//    isolation= true
    vmOptions.set(
        mapOf("kotlin-logging-to-android-native" to "true")
    )
//    args.set("sleep=true")
}
