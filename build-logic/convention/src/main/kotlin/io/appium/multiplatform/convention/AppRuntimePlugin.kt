package io.appium.multiplatform.convention

import com.android.adblib.*
import com.android.adblib.utils.AdbProtocolUtils
import com.android.adblib.utils.JdkLoggerFactory
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.tasks.PackageApplication
import io.appium.multiplatform.selectOnlineDevice
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

interface AppRuntimePluginExtension {
    val mainClass: Property<String?>
    val androidSerial: Property<String?>

    companion object {
        const val NAME = "appRuntime"
    }
}

/**
 * AppRuntime(app_process) plugin for run apk
 *
 * Configuration order: task cli option > Env > Gradle Property > PluginExtension
 * Naming convention (based on name, you can tell the source of the config): android-serial > ANDROID_SERIAL > android-serial-property > androidSerial
 *
 * Configuration does not support fallback; once a non-null value is found according to the priority, it takes effect.
 */
abstract class AppRuntimePlugin @Inject constructor(val project: Project) : Plugin<Project> {

    val appRuntimePluginExtension: AppRuntimePluginExtension by lazy {
        project.extensions.create(AppRuntimePluginExtension.NAME, AppRuntimePluginExtension::class.java)
    }
    private val androidSerial: Pair<String, String>? by lazy {
        sequenceOf(
            "ANDROID_SERIAL" to { System.getenv("ANDROID_SERIAL") },
            "android-serial-property" to { project.findProperty("android-serial-property")?.toString() },
            "androidSerial" to { appRuntimePluginExtension.androidSerial.orNull }
        )
            .mapNotNull { (source, valueProvider) ->
                val value = valueProvider()
                if (!value.isNullOrBlank()) {
                    source to value
                } else {
                    null
                }
            }
            .firstOrNull()
    }
    private val mainClass: Pair<String, String>? by lazy {
        sequenceOf(
            "MAIN_CLASS" to { System.getenv("MAIN_CLASS") },
            "main-class-property" to { project.findProperty("main-class-property")?.toString() },
            "mainClass" to { appRuntimePluginExtension.mainClass.orNull }
        )
            .mapNotNull { (source, valueProvider) ->
                val value = valueProvider()
                if (!value.isNullOrBlank()) {
                    source to value
                } else {
                    null
                }
            }
            .firstOrNull()
    }

    override fun apply(target: Project) {
        appRuntimePluginExtension.mainClass.convention(null)
        appRuntimePluginExtension.androidSerial.convention(null)
        if (target.plugins.hasPlugin("com.android.application")) {
            target.configureAppRuntime()
        }
    }


    fun Project.configureAppRuntime() {
        plugins.findPlugin(AppPlugin::class.java)?.apply {
            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants { variant ->
                    val variantName = variant.name.replaceFirstChar { it.uppercase() }
                    val apks = project.objects.listProperty(RegularFile::class.java)
                    variant.resolveApks(apks)
                    logger.info("findProperty: ${project.findProperty("serial")}")
                    tasks.register<AppRuntimeRunTask>("appRuntimeRun${variantName}") {
                        val packageTask =
                            tasks.named("package${variantName}", PackageApplication::class.java)
                        dependsOn(packageTask)
                        apkFiles.set(apks)
                        this@AppRuntimePlugin.mainClass?.let {
                            logger.info("found mainClass, source:${it.first}, value:${it.second}") // If this log is missing, the parameter came from the task’s CLI.
                            this.mainClass = it.second
                        }
                        this@AppRuntimePlugin.androidSerial?.let {
                            logger.info("found androidSerial, source:${it.first}, value:${it.second}") // If this log is missing, the parameter came from the task’s CLI.
                            this.androidSerial = it.second
                        }
                    }
                }
            }
        }
    }
}

interface AppRuntimeWorkParameters : WorkParameters {
    val apkFiles: ListProperty<RegularFile>

    val serial: Property<String>
    val mainClass: Property<String>
}


abstract class AppRuntimeWorkAction : WorkAction<AppRuntimeWorkParameters> {

    private val _defaultLogger: AdbLogger by lazy {
        JdkLoggerFactory.JdkLogger(AppRuntimeWorkAction::class.java.simpleName)
    }

    private val _deviceLogger: AdbLogger by lazy {
        adbLogger(device.session).withDevicePrefix(device)
    }
    private val logger: AdbLogger
        get() = if (::device.isInitialized) {
            _deviceLogger
        } else {
            _defaultLogger
        }

    lateinit var apkFile: File

    lateinit var device: ConnectedDevice
    private val remoteFilePath: Path
        get() = REMOTE_DIR_PATH.resolve(apkFile.name)

    /**
     *
     * 因为权限限制和时间精度不一致，[com.android.adblib.FileStat]和[com.android.adblib.RemoteFileMode]都不能用来判断
     */
    private suspend fun checkUpdate(): Boolean {
        val localPath = apkFile.toPath()
        val localSize = apkFile.length()
        val localTime = Files.getLastModifiedTime(localPath)

        return device.fileSystem.withSyncServices { service ->
            val remoteStat = try {
                service.stat(remoteFilePath.toString())
            } catch (e: IOException) {
                logger.error(e, "Failed to stat remote file '$remoteFilePath', assuming update is needed.")
                return@withSyncServices true
            }
            if (remoteStat == null) {
                logger.debug { "Remote file $remoteFilePath does not exist. Update needed." }
                return@withSyncServices true
            }

            // Compare file sizes.
            // Note: AdbLib returns size as Int, which might be an issue for files > 2GB.
            if (remoteStat.size.toLong() != localSize) {
                logger.debug { "File size mismatch for $remoteFilePath. Local: $localSize, Remote: ${remoteStat.size}. Update needed." }
                return@withSyncServices true
            }

            // Compare modification times (second precision).
            val remoteTimeEpoch = AdbProtocolUtils.convertFileTimeToEpochSeconds(remoteStat.lastModified)
            val localTimeEpoch = AdbProtocolUtils.convertFileTimeToEpochSeconds(localTime)
            if (remoteTimeEpoch != localTimeEpoch) {
                logger.debug {
                    "File timestamp mismatch for $remoteFilePath. Local: $localTimeEpoch, Remote: $remoteTimeEpoch. Update needed."
                }
                return@withSyncServices true
            }

            // If all checks pass, no update is needed.
            false
        }
    }

    suspend fun update() {
        if (checkUpdate()) {
            logger.info { "push file `${apkFile.absolutePath}` to `$remoteFilePath`" }
            device.fileSystem.sendFile(
                apkFile.toPath(),
                remoteFilePath.toString(),
                RemoteFileMode.DEFAULT,
                Files.getLastModifiedTime(apkFile.toPath()),
                null,
                SYNC_DATA_MAX
            )
        } else {
            logger.info { "no need to update" }
        }
    }

    suspend fun start() {
        val cmd = buildList {
            add("app_process")
            add("-cp")
            add(remoteFilePath)
            add("/data/local/tmp")
            add("--application")
            parameters.mainClass.orNull?.let { add(it) }
        }.joinToString(" ")
        logger.info { "cmd: `$cmd`" }
        device.shell.executeAsLines(cmd, null, 60.seconds.toJavaDuration())
            .collect { line ->
                when (line) {
                    is ShellCommandOutputElement.StdoutLine -> {
                        logger.info { "stdout: $line" }
                    }

                    is ShellCommandOutputElement.StderrLine -> {
                        logger.info { "stderr: $line" }
                    }

                    is ShellCommandOutputElement.ExitCode -> {
                        logger.info { "exit code: $line" }
                    }
                }
            }
    }

    suspend fun stop() {
        val pids = mutableSetOf<Int>()
        device.shell.executeAsLines("ps -ef").collect { line ->
            when (line) {
                is ShellCommandOutputElement.StdoutLine -> {
                    if ("app_process" in line.contents && apkFile.name in line.contents) {
                        pids.add(line.contents.split(Regex("\\s+")).firstNotNullOf { it.toIntOrNull() })
                    }
                    logger.info { "stdout: ${line.contents.split(" ")}" }
                }

                is ShellCommandOutputElement.StderrLine -> {
                    logger.info { "stderr: $line" }
                }

                is ShellCommandOutputElement.ExitCode -> {
                    logger.info { "exit code: $line" }
                }
            }
        }
        logger.info { "pids: $pids" }
    }

    fun forceStop() {}
    override fun execute() {
        apkFile = parameters.apkFiles.get().map { it.asFile }.firstOrNull { it.exists() && it.isFile }
            ?: throw GradleException("No valid APK file found in the inputs.")

        runBlocking {
            AdbSessionHost().use { host ->
                AdbSession.create(host, connectionTimeout = 10.seconds.toJavaDuration()).use { session ->
                    logger.info { "serverStatus: ${session.hostServices.serverStatus()}" }
                    device = session.selectOnlineDevice(parameters.serial.orNull)
                    logger.info { "connectedDevice: $device" }
                    update()
                    try {
                        device.withScopeContext {
                            start()
                        }.withRetry {
                            logger.warn(it, "withRetry(false)")
                            false
                        }
                            .withFinally {
                                logger.info { "finished" }
                            }.execute()
                    } catch (e: CancellationException) {
                        logger.info(e) { "cancelled" }
                    }

                }
            }
        }
    }

    companion object {
        val REMOTE_DIR_PATH = Path("/data/local/tmp")
    }
}

abstract class AppRuntimeRunTask @Inject constructor() : DefaultTask() {
    init {
        group = "run"
        description = "Android application process launcher (app_process)"
    }

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @get:InputFiles
    abstract val apkFiles: ListProperty<RegularFile>

    @set:Option(
        option = "main-class",
        description = "The fully qualified name of the Java class to execute.This class must have a main(String[] args) method."
    )
    @Input
    @Optional
    var mainClass: String? = null

    @set:Option(option = "android-serial", description = "ADB device serial (overrides `ANDROID_SERIAL`)")
    @Input
    @Optional
    var androidSerial: String? = null

    @TaskAction
    fun runTask() {
        workerExecutor.noIsolation().submit(AppRuntimeWorkAction::class.java) {
            apkFiles.set(this@AppRuntimeRunTask.apkFiles)
            serial.set(this@AppRuntimeRunTask.androidSerial)
            mainClass.set(this@AppRuntimeRunTask.mainClass)
        }
    }
}
