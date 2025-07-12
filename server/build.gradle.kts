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
            }
        }
        commonTest {
            dependencies {

            }
        }
        androidMain {
            dependencies {
//                implementation(libs.uiautomator)
//                implementation("androidx.test:runner:1.7.0-alpha03")
                implementation("androidx.annotation:annotation:1.9.1")
                implementation(libs.adblib)
                implementation(libs.hiddenapibypass)
            }
        }
        androidInstrumentedTest {
            dependencies {
                implementation(libs.uiautomator)
                implementation("androidx.test:orchestrator:1.6.0-beta01")
                implementation("androidx.test:runner:1.7.0-beta01")
                implementation("androidx.test:rules:1.7.0-beta01")
                implementation("androidx.test.ext:junit-ktx:1.3.0-beta01")
                implementation("androidx.test:core-ktx:1.7.0-beta01")
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.adblib)
                implementation(libs.slf4j.simple)
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
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

appRuntime {
    mainClass = "io.appium.multiplatform.server.DemoKt"
//    isolation= true
//    androidSerial = "6b410050"
}
