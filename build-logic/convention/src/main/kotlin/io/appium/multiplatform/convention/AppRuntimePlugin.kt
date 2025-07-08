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
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
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

/**
 * Extension for configuring the [AppRuntimePlugin].
 */
interface AppRuntimePluginExtension {
    /** The fully qualified name of the main class to execute. */
    val mainClass: Property<String?>

    /** The serial number of the target Android device. */
    val androidSerial: Property<String?>

    /** Whether to run the task in isolation. */
    val isolationTask: Property<Boolean?>

    companion object {
        const val NAME = "appRuntime"
    }
}

/**
 * A Gradle plugin to run an application on an Android device using `app_process`.
 *
 * This plugin provides tasks to deploy and launch an APK's main class on a connected device.
 * It resolves configuration in the following order of precedence:
 * 1. Task command-line options (e.g., `--main-class`, `--android-serial`)
 * 2. Environment variables (e.g., `MAIN_CLASS`, `ANDROID_SERIAL`)
 * 3. Gradle properties (e.g., `main-class-property`, `android-serial-property`)
 * 4. Plugin extension block in `build.gradle.kts` (e.g., `appRuntime { ... }`)
 *
 * Configuration does not fall back; the first non-null value found is used.
 */
abstract class AppRuntimePlugin @Inject constructor(val project: Project, val objectFactory: ObjectFactory) :
    Plugin<Project> {

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

    /**
     * Applies the plugin to the given project.
     * It sets up default conventions and configures the app runtime tasks if the Android application plugin is present.
     */
    override fun apply(target: Project) {
        appRuntimePluginExtension.mainClass.convention(null)
        appRuntimePluginExtension.androidSerial.convention(null)
        appRuntimePluginExtension.isolationTask.convention(false)
        if (target.plugins.hasPlugin("com.android.application")) {
            target.configureAppRuntime()
        }
    }


    /**
     * Configures the `appRuntimeRun` task for each application variant.
     * This task is responsible for deploying and running the application using `app_process`.
     */
    fun Project.configureAppRuntime() {
        plugins.findPlugin(AppPlugin::class.java)?.apply {
            extensions.configure<ApplicationAndroidComponentsExtension> {
                onVariants { variant ->
                    val variantName = variant.name.replaceFirstChar { it.uppercase() }
                    tasks.register<AppRuntimeRunTask>("appRuntimeRun${variantName}") {
                        if (!appRuntimePluginExtension.isolationTask.get()) {
                            tasks.named("package${variantName}", PackageApplication::class.java).let {
                                dependsOn(it)
                            }
                        }
                        apk.set(variant.resolveApk())
                        this@AppRuntimePlugin.mainClass?.let {
                            logger.info("found mainClass, source:${it.first}, value:${it.second}") // If this log is missing, the parameter came from the task’s CLI.
                            mainClass = it.second
                        }
                        this@AppRuntimePlugin.androidSerial?.let {
                            logger.info("found androidSerial, source:${it.first}, value:${it.second}") // If this log is missing, the parameter came from the task’s CLI.
                            androidSerial = it.second
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parameters for the [AppRuntimeWorkAction].
 */
interface AppRuntimeWorkParameters : WorkParameters {
    val apk: RegularFileProperty
    val serial: Property<String>
    val mainClass: Property<String>
}


/**
 * A Gradle WorkAction that executes the application on a target device using `app_process`.
 * This action handles pushing the APK, executing the main class, and cleaning up.
 */
abstract class AppRuntimeWorkAction : WorkAction<AppRuntimeWorkParameters> {
    var logger: AdbLogger = JdkLoggerFactory.JdkLogger(AppRuntimeWorkAction::class.java.simpleName)

    lateinit var apkFile: File

    private val remoteFilePath: Path
        get() = REMOTE_DIR_PATH.resolve(apkFile.name)

    /**
     * Checks if the APK on the remote device needs to be updated.
     * The check is based on file size and modification time.
     *
     * Note: This check does not use [com.android.adblib.FileStat] or [com.android.adblib.RemoteFileMode]
     * for comparison due to permission limitations and inconsistencies in time precision.
     *
     * @param device The connected device.
     * @return `true` if an update is needed, `false` otherwise.
     */
    private suspend fun checkUpdate(device: ConnectedDevice): Boolean {
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

    /**
     * Pushes the APK to the device if it needs to be updated.
     * @param device The connected device.
     */
    suspend fun update(device: ConnectedDevice) {
        if (checkUpdate(device)) {
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

    /**
     * Starts the application on the device using the `app_process` command.
     * @param device The connected device.
     */
    suspend fun start(device: ConnectedDevice) {
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
        //TODO: kill by pid
    }

    /**
     * Finds and logs the process IDs (PIDs) of the running `app_process` for the current APK.
     *
     * @param device The connected device.
     */
    suspend fun stop(device: ConnectedDevice) {
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

    fun forceStop() {
        TODO("not implemented.")
    }

    /**
     * The main entry point for the work action.
     * It establishes a connection to the device, updates the APK, and runs the application.
     */
    override fun execute() {
        runBlocking {
            apkFile = parameters.apk.get().asFile
            AdbSessionHost().use { host ->
                AdbSession.create(host, connectionTimeout = 10.seconds.toJavaDuration()).use { session ->
                    logger.info { "serverStatus: ${session.hostServices.serverStatus()}" }
                    with(session.selectOnlineDevice(parameters.serial.orNull)) {
                        logger.info { "connectedDevice: $this" }
                        adbLogger(this.session).withDevicePrefix(this)//update device logger
                        update(this)
                        try {
                            withScopeContext {
                                start(this@with)
                            }.withRetry {
                                logger.warn(it, "withRetry(false)")
                                false
                            }.withFinally {
                                logger.info { "finished" }
                            }.execute()
                        } catch (e: CancellationException) {
                            logger.info(e) { "cancelled" }
                        } catch (e: RuntimeException) {
                            logger.info(e) { "cancelled RuntimeException" }
                        }
                    }
                }
            }
        }
    }

    companion object {
        val REMOTE_DIR_PATH = Path("/data/local/tmp")
    }
}

/**
 * A Gradle task that launches an Android application via `app_process`.
 * It uses a [WorkerExecutor] to run the deployment and execution logic in a separate process.
 */
abstract class AppRuntimeRunTask @Inject constructor() : DefaultTask() {
    init {
        group = "run"
        description = "Android application process launcher (app_process)"
    }

    @get:Inject
    abstract val workerExecutor: WorkerExecutor

    @get:InputFile
    abstract val apk: RegularFileProperty

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

    /**
     * The main action of the task. It submits the [AppRuntimeWorkAction] to the worker executor.
     */
    @TaskAction
    fun runTask() {
        workerExecutor.noIsolation().submit(AppRuntimeWorkAction::class.java) {
            serial.set(this@AppRuntimeRunTask.androidSerial)
            mainClass.set(this@AppRuntimeRunTask.mainClass)
            apk.set(this@AppRuntimeRunTask.apk)
        }
    }
}
