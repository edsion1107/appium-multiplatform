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
tasks.register("installDist") {
    dependsOn(":server:assembleDebug")

    doLast {
        AdbSessionHost().use { host ->
            host.adbLogger(JdkLoggerFactory())
            AdbSession.create(host).use { session ->
                session.adbLogger(JdkLoggerFactory())
                runBlocking {
                    val job = session.createDeviceScope(DeviceSelector.any()).launch {
                        session.hostServices.devices(AdbHostServices.DeviceInfoFormat.LONG_FORMAT).first {
                            session.host.logger.debug { "deviceInfo: $it" }
                            it.deviceState == DeviceState.ONLINE
                        }
                        //session.connectedDevicesTracker.device(DeviceSelector.any())?.waitUntilOnline()

                        val device = session.connectedDevicesTracker.connectedDevices.first().first { it.isOnline }
                        device.withScopeContext {
                            val sourceFilePath =
                                Paths.get("/Users/edsion/repo/appium-multiplatform/server/build/outputs/apk/debug/server-debug-v1.0.0-SNAPSHOT.apk")

                            device.fileSystem.sendFile(
                                sourcePath = sourceFilePath,
                                remoteFilePath = "/data/local/tmp/${sourceFilePath.fileName}",
                                remoteFileMode = RemoteFileMode.DEFAULT,
                                remoteFileTime = getLastModifiedTime(sourceFilePath),
                                progress = object : SyncProgress {
                                    private val logger = host.logger.withDevicePrefix(device)
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
                        }.execute()
                    }
                    job.join()
                }
            }
        }

    }
}

tasks.register("installDebugDist") {
    dependsOn(tasks.withType(PackageApplication::class.java))
    doLast {
        val sourceFilePath =
            Paths.get("/Users/edsion/repo/appium-multiplatform/server/build/outputs/apk/debug/server-debug-v1.0.0-SNAPSHOT.apk")


        AdbSessionHost().use { host ->
            AdbSession.create(host).use { session ->
                session.adbLogger(JdkLoggerFactory())
                runBlocking {
                    val deviceInfo =
                        session.hostServices.devices(format = AdbHostServices.DeviceInfoFormat.BINARY_PROTO_FORMAT)
                            .onEach { session.host.logger.info { "deviceInfo: $it" } }
                            .firstOrNull { it.deviceState == DeviceState.ONLINE }
                            ?: throw GradleException("no device found")
                    val connectDevice = session.connectedDevicesTracker.waitForDevice(deviceInfo.serialNumber)
                    connectDevice.withScopeContext {
                        connectDevice.waitUntilOnline()
                        connectDevice.fileSystem.sendFile(
                            sourcePath = sourceFilePath,
                            remoteFilePath = "/data/local/tmp/${sourceFilePath.fileName}",
                            remoteFileMode = RemoteFileMode.DEFAULT,
                            remoteFileTime = getLastModifiedTime(sourceFilePath),
                            progress = null,
                            bufferSize = SYNC_DATA_MAX
                        )
                        logger.info("push file success")
                    }.withRetry { e ->
                        logger.error("push file failed", e)
                        false
                    }
                        .withFinally {
                            logger.quiet("push file $sourceFilePath to /data/local/tmp/${sourceFilePath.fileName} finished")
                        }.execute()
                }
            }
        }
    }
}
