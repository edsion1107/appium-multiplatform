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
                implementation(libs.uiautomator)
                implementation("androidx.test:runner:1.7.0-alpha03")
                implementation(libs.adblib)
            }
        }
        jvmMain {
            dependencies {
                implementation(libs.adblib)
            }
        }
    }
}
android {
    namespace = "com.appium.multiplatform"
}

appRuntime {
    mainClass = "io.appium.multiplatform.server.DemoKt"
//    isolation= true
//    androidSerial = "6b410050"
}
