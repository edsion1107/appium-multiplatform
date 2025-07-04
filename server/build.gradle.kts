import com.android.adblib.*
import com.android.adblib.utils.JdkLoggerFactory
import com.android.build.gradle.tasks.PackageApplication
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.Files.getLastModifiedTime
import java.nio.file.Paths

buildscript {
    dependencies {
        classpath(libs.adblib)
    }
}

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.android.convention.plugin)
    alias(libs.plugins.android.app.process.plugin)
}

group = "io.appium.multiplatform"
version = "unspecified"


kotlin {
    androidTarget ()
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
        jvmMain{
            dependencies {
                implementation(libs.adblib)
            }
        }
    }
}
android {
    namespace = "com.appium.multiplatform"
}

suspend fun sendFile(device: ConnectedDevice) {
    val sourceFilePath =
        Paths.get("/Users/edsion/repo/appium-multiplatform/server/build/outputs/apk/debug/server-debug-v1.0.0-SNAPSHOT.apk")

    device.fileSystem.sendFile(
        sourcePath = sourceFilePath,
        remoteFilePath = "/data/local/tmp/${sourceFilePath.fileName}",
        remoteFileMode = RemoteFileMode.DEFAULT,
        remoteFileTime = getLastModifiedTime(sourceFilePath),
        progress = object : SyncProgress {
            private val logger = device.session.host.logger.withDevicePrefix(device)
            override suspend fun transferStarted(remotePath: String) {
                logger.info { "Sync Started $remotePath" }
            }

            override suspend fun transferProgress(
                remotePath: String,
                totalBytesSoFar: Long
            ) {
                logger.debug { "Sync Progress $remotePath: $totalBytesSoFar" }
            }

            override suspend fun transferDone(
                remotePath: String,
                totalBytes: Long
            ) {
                logger.info { "Sync Done $remotePath" }
            }

        }
    )
}
