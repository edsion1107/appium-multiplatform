package io.appium.multiplatform.convention

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.LoggerWrapper
import com.android.build.gradle.internal.tasks.InstallVariantTask
import com.android.build.gradle.internal.testing.ConnectedDeviceProvider
import com.android.ddmlib.MultiLineReceiver
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.provider.MapProperty
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
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Extension for configuring the [AppRuntimePlugin].
 */
interface AppRuntimePluginExtension {
    /** The fully qualified name of the main class to execute. */
    val mainClass: Property<String>

    /** Whether to run the task in isolation. */
    val isolation: Property<Boolean>

    /** Java VM option and will be passed to the Android Runtime. */
    val vmOptions: MapProperty<String, String>

    /** string arguments to the main method of the executed class. */
    val args: Property<String> //args may involve the composition of many parameters, you can't use map and conversion like vmOptions, just pass it in the original string.

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
abstract class AppRuntimePlugin @Inject constructor(val project: Project) :
    Plugin<Project> {

    val appRuntimePluginExtension: AppRuntimePluginExtension by lazy {
        project.extensions.create(AppRuntimePluginExtension.NAME, AppRuntimePluginExtension::class.java)
    }.apply {
        value.mainClass.convention(null)
        value.isolation.convention(false)
        value.vmOptions.convention(emptyMap())
        value.args.convention(null)
    }
    private val mainClass: Pair<String, String>? by lazy {
        sequenceOf(
            "MAIN_CLASS" to { System.getenv("MAIN_CLASS") },
            "main-class-property" to { project.findProperty("main-class-property")?.toString() },
            "mainClass" to { appRuntimePluginExtension.mainClass.orNull }
        )
            .mapNotNull { (source, valueProvider) ->
                valueProvider()?.takeIf { it.isNotBlank() }?.let { source to it }
            }
            .firstOrNull()
    }
    private val vmOptions: Pair<String, MutableMap<String, String>>? by lazy {
        sequenceOf(
            "VM_OPTIONS" to { parseKeyValueString(System.getenv("VM_OPTIONS"), project.logger) },
            "vm-options-property" to {
                parseKeyValueString(
                    project.findProperty("vm-options-property")?.toString(),
                    project.logger
                )
            },
            "vmOptions" to { appRuntimePluginExtension.vmOptions.orNull }
        ).mapNotNull { (source, valueProvider) ->
            valueProvider()?.takeIf { it.isNotEmpty() }?.let { source to it }
        }
            .firstOrNull()
    }
    private val args: Pair<String, String>? by lazy {
        sequenceOf(
            "ARGS" to { System.getenv("ARGS") },
            "args-property" to { project.findProperty("args-property")?.toString() },
            "args" to { appRuntimePluginExtension.args.orNull }
        )
            .mapNotNull { (source, valueProvider) ->
                valueProvider()?.takeIf { it.isNotBlank() }?.let { source to it }
            }
            .firstOrNull()
    }

    /**
     * Applies the plugin to the given project.
     * It sets up default conventions and configures the app runtime tasks if the Android application plugin is present.
     */
    override fun apply(target: Project) {
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
                        // Note: If there is also an input in the task that corresponds to the outputDirectory of PackageApplication (PackageAndroidArtifact), this will make the tasks implicitly dependent.
                        // Maybe this is a good practice, but it doesn't achieve the isolation mode I want for easy independent debugging.
                        if (appRuntimePluginExtension.isolation.orNull != true) {
                            dependsOn(tasks.named("install${variantName}", InstallVariantTask::class.java))
                        }
                        applicationId.set(variant.applicationId)
                        adbLocation.set(sdkComponents.adb)

                        // Note: The parameters mainClass, vmOptions, and args below come from different scopes.
                        // Kotlin's qualified this syntax (this@AppRuntimePlugin) is used here to disambiguate.
                        this@AppRuntimePlugin.mainClass?.let {
                            logger.info("found mainClass, source:${it.first}, value:${it.second}") // If this log is missing, the parameter came from the task’s CLI.
                            mainClassParam = it.second
                        }
                        this@AppRuntimePlugin.vmOptions?.let {
                            logger.info("found vmOptions, source:${it.first}, value:${it.second}")
                            vmOptions.set(it.second)
                        }
                        this@AppRuntimePlugin.args?.let {
                            logger.info("found args, source:${it.first}, value:${it.second}")
                            argsParam = it.second
                        }
                    }
                }
                finalizeDsl { dsl ->
                    dsl.buildTypes.forEach { buildType ->
                        if (buildType.signingConfig == null) {
                            // for example, `./gradlew projectReport` will throw TaskExecutionException: `Could not create task ':server:appRuntimeRunRelease'. > Task with name 'installRelease' not found in project ':server'.`
                            logger.error(
                                "signingConfig not set for buildType:`${buildType.name}`. AppRuntimeRunTask dependsOn this.\n" +
                                        "\tadd signingConfig or set `isolation= true`"
                            )
                        }
                    }
                }
            }
        }
    }


}

private fun parseKeyValueString(input: String?, logger: Logger): MutableMap<String, String> {
    if (input == null) {
        return mutableMapOf()
    }
    val result = mutableMapOf<String, String>()
    input.split(",").forEachIndexed { index, entry ->
        val parts = entry.split("=", limit = 2)

        if (parts.size != 2) {
            logger.warn("Entry at index $index ('$entry') is invalid and will be skipped.")
            return@forEachIndexed
        }

        val key = parts[0].trim()
        val value = parts[1].trim()

        if (key.isEmpty() || value.isEmpty()) {
            logger.warn("Empty key or value at index $index ('$entry') will be skipped.")
            return@forEachIndexed
        }

        if (result.containsKey(key)) {
            logger.warn("Duplicate key '$key' found at index $index. Overwriting previous value.")
        }

        result[key] = value
    }

    return result
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
    abstract val adbLocation: RegularFileProperty

    @get:Input
    abstract val applicationId: Property<String>

    @set:Option(
        option = "main-class",
        description = "The fully qualified name of the Java class to execute.This class must have a main(String[] args) method."
    )
    @Input
    @Optional
    var mainClassParam: String? = null

    @set:Option(
        option = "vmOptions",
        description = "Java VM option and will be passed to the Android Runtime.Common examples defining system properties (-D)."
    )
    @Input
    @Optional
    var vmOptionsParam: String? = null

    @get:Input
    @get:Optional
    abstract val vmOptions: MapProperty<String, String>

    @set:Option(
        option = "args",
        description = "string arguments to the main method of the executed class."
    )
    @Input
    @Optional
    var argsParam: String? = null

    /**
     * The main action of the task. It submits the [AppRuntimeWorkAction] to the worker executor.
     */
    @TaskAction
    fun runTask() {
        workerExecutor.noIsolation().submit(AppRuntimeWorkAction::class.java) {
            mainClass.set(mainClassParam)
            val options = parseKeyValueString(vmOptionsParam, logger)
            if (options.isNotEmpty()) {
                vmOptions.set(options)
            } else {
                vmOptions.set(this@AppRuntimeRunTask.vmOptions)
            }
            args.set(this@AppRuntimeRunTask.argsParam)
            applicationId.set(this@AppRuntimeRunTask.applicationId)
            adbLocation.set(this@AppRuntimeRunTask.adbLocation)
        }
    }
}

/**
 * Parameters for the [AppRuntimeWorkAction].
 */
interface AppRuntimeWorkParameters : WorkParameters {
    val mainClass: Property<String>
    val vmOptions: MapProperty<String, String>
    val args: Property<String>
    val applicationId: Property<String>
    val adbLocation: RegularFileProperty
}


/**
 * A Gradle WorkAction that executes the application on a target device using `app_process`.
 * This action handles pushing the APK, executing the main class, and cleaning up.
 */
abstract class AppRuntimeWorkAction : WorkAction<AppRuntimeWorkParameters> {
    val logger = LoggerWrapper(Logging.getLogger(AppRuntimeWorkAction::class.java))

    val cmd by lazy {
        buildList {
            add("app_process")
            classPath?.let { add(it) }
            if (vmOptions?.isNotEmpty() == true) {
                vmOptions?.forEach { add(it) }
            }
            cmdDir?.let { add(it) }
            if (isApplication) {
                add("--application")
            }
            mainCLass?.let { add(it) }
            args?.let { add(it) }
        }.joinToString(" ")
    }

    val mainCLass: String? by lazy {
        parameters.mainClass.orNull
    }
    val vmOptions: List<String>? by lazy {
        parameters.vmOptions.get().map { (k, v) -> "-D$k=$v" }
    }
    val args: String? by lazy {
        parameters.args.orNull
    }
    val classPath: String? by lazy {
        parameters.applicationId.orNull.let { "-cp $(pm path $it)" }
    }
    val cmdDir: String? by lazy {
        "/data/local/tmp"
    }
    val isApplication: Boolean = true
    var hasOutput: Int =
        0  // ddmlib does not provide stdout, stderr, and return code, so exceptions are only judged by whether there is output (none is present when normal).
    val logReceiver: MultiLineReceiver = object : MultiLineReceiver() {
        override fun processNewLines(lines: Array<out String?>?) {
            lines.orEmpty().filterNotNull().filter(String::isNotBlank)
                .forEach { line ->
                    logger.warning(line)
                    hasOutput++
                }
        }

        override fun isCancelled() = false

    }


    override fun execute() {
        val connectedDeviceProvider = ConnectedDeviceProvider(
            parameters.adbLocation,
            5_000,
            logger,
            System.getenv("ANDROID_SERIAL")
        )
        connectedDeviceProvider.use {
            @Suppress("UnstableApiUsage")
            connectedDeviceProvider.devices.mapNotNull { it as? com.android.build.gradle.internal.testing.ConnectedDevice }
                .forEach { connectedDevice ->
                    logger.lifecycle("device: ${connectedDevice.name}, sn: ${connectedDevice.serialNumber}, state: ${connectedDevice.state}, command: $cmd")
                    connectedDevice.executeShellCommand(cmd, logReceiver, 0, 0, TimeUnit.SECONDS)
                }
            if (hasOutput != 0) {
                throw GradleException("An unknown error occurred. Please add --info to view details.")
            }
        }
    }
}
