plugins {
    alias(libs.plugins.project.report)
    alias(libs.plugins.kotlin.multiplatform)
//    alias(libs.plugins.android.kotlin.multiplatform.library)
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
            // only android required, no need jvm.  Notification: Duplicate content roots detected
            resources.srcDirs("src/commonMain/resources")
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
        mapOf("kotlin-logging-to-android-native" to "true")
    )
//    args.set("sleep=true")
}
