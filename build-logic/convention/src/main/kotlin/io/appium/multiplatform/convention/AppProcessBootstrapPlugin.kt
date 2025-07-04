package io.appium.multiplatform.convention

import com.android.adblib.AdbSession
import com.android.adblib.AdbSessionHost
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.PackageApplication
import io.appium.multiplatform.selectOnlineDevice
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


abstract class AppProcessBootstrapPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target.plugins.hasPlugin("com.android.application")) {
            target.configureAppProcessBootstrap()
        }
    }

    fun Project.configureAppProcessBootstrap() {
        plugins.findPlugin(AppPlugin::class.java)?.apply {
            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants { variant ->
                    val variantName = variant.name.replaceFirstChar { it.uppercase() }
                    val apks = project.objects.listProperty(RegularFile::class.java)
                    variant.resolveApks(apks)

                    tasks.register<AppProcessRunTask>("AppProcessRun${variantName}") {
                        val packageTask =
                            tasks.named("package${variantName}", PackageApplication::class.java)
                        dependsOn(packageTask)
                        apkFiles.set(apks)
                    }
                }
            }
        }
    }
}

interface AppProcessWorkParameters : WorkParameters {
    val apkFiles: ListProperty<RegularFile>
    val serial: Property<String>
}

abstract class AppProcessWorkAction : WorkAction<AppProcessWorkParameters> {
    private val logger = Logging.getLogger(AppProcessWorkAction::class.java)


    override fun execute() {
        val apk = parameters.apkFiles.get().map { it.asFile }.firstOrNull { it.exists() && it.isFile }
            ?: throw GradleException("No valid APK file found in the inputs.")
        logger.info("apkFile: ${apk.absolutePath}")

        runBlocking {
            AdbSessionHost().use { host ->
                AdbSession.create(host, connectionTimeout = 10.seconds.toJavaDuration()).use { session ->
                    val connectedDevice = session.selectOnlineDevice(parameters.serial.orNull)
                    connectedDevice.session.host.logger.info { "connectedDevice: $connectedDevice" }
                }
            }
        }
    }
}

abstract class AppProcessRunTask @Inject constructor() : DefaultTask() {
    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @get:InputFiles
    abstract val apkFiles: ListProperty<RegularFile>
    private val serial: String? by lazy {
        System.getenv("ANDROID_SERIAL")
            ?: System.getProperty("ANDROID_SERIAL")
            ?: project.findProperty("ANDROID_SERIAL")?.toString()
    }

    @TaskAction
    fun runTask() {
        workerExecutor.noIsolation().submit(AppProcessWorkAction::class.java) {
            apkFiles.set(this@AppProcessRunTask.apkFiles)
            serial.set(this@AppProcessRunTask.serial)
        }
    }
}
