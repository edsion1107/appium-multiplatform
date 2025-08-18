package io.appium.multiplatform.service

import android.annotation.SuppressLint
import android.app.IInstrumentationWatcher
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.appium.multiplatform.jvm.ReflectiveMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lsposed.hiddenapibypass.HiddenApiBypass
import org.lsposed.hiddenapibypass.LSPass

@SuppressLint("PrivateApi")
object UiDeviceProvider {
    init {
        @Suppress("DEPRECATION")
        Looper.prepareMainLooper()  // fix: Can't create handler inside thread, that has not called Looper.prepare()
    }

    const val UI_AUTOMATION_CONNECTION_CLASS = "android.app.UiAutomationConnection"
    const val ACTIVITY_THREAD_CLASS = "android.app.ActivityThread"
    private val logger = KotlinLogging.logger {}
    val componentName = ComponentName("com.appium.multiplatform.server", "androidx.test.runner.AndroidJUnitRunner")
    val instrumentation = Instrumentation()

    private val activityThread by lazy {
        runCatching {
            LSPass.invoke(Class.forName(ACTIVITY_THREAD_CLASS), null, "systemMain")
        }.recoverCatching {
            HiddenApiBypass.invoke(Class.forName(ACTIVITY_THREAD_CLASS), null, "systemMain")
        }
            .onFailure {
                logger.error(it) { "ActivityThread not initialized" }
            }.getOrThrow()
    }
    private val systemContext by lazy {
        runCatching {
            LSPass.invoke(
                Class.forName(ACTIVITY_THREAD_CLASS),
                activityThread,
                "getSystemContext"
            ) as Context
        }.recoverCatching {
            HiddenApiBypass.invoke(
                Class.forName(ACTIVITY_THREAD_CLASS),
                activityThread,
                "getSystemContext"
            ) as Context
        }.onFailure {
            logger.error(it) { "SystemContext not initialized" }
        }.getOrThrow()
    }
    private val uiAutomationConnection by lazy {
        runCatching {
            LSPass.newInstance(Class.forName(UI_AUTOMATION_CONNECTION_CLASS))
        }.recoverCatching {
            HiddenApiBypass.newInstance(Class.forName(UI_AUTOMATION_CONNECTION_CLASS))
        }.onFailure {
            logger.error(it) { "uiAutomationConnection not initialized" }
        }.getOrThrow()
    }

    private val instrumentationWatcher = object : IInstrumentationWatcher.Stub() {
        override fun instrumentationStatus(
            name: ComponentName?,
            resultCode: Int,
            results: Bundle?
        ) {
            logger.info { "instrumentationStatus,name:$name,resultCode:$resultCode,results:$results" }
        }

        override fun instrumentationFinished(
            name: ComponentName?,
            resultCode: Int,
            results: Bundle?
        ) {
            logger.info { "instrumentationStatus,name:$name,resultCode:$resultCode,results:$results" }
        }
    }

    fun initialize() {
        val initMethod = ReflectiveMethod<Instrumentation>(
            Instrumentation::class.java,
            "init",
            Class.forName(ACTIVITY_THREAD_CLASS),
            Context::class.java,
            Context::class.java,
            ComponentName::class.java,
            Class.forName("android.app.IInstrumentationWatcher"),
            Class.forName("android.app.IUiAutomationConnection")
        )
        initMethod.invoke(
            instrumentation, activityThread, systemContext,
            systemContext,
            componentName,
            instrumentationWatcher,
            uiAutomationConnection
        )
        instrumentation.start()
    }

    @SuppressLint("ObsoleteSdkInt")
    fun get(arguments: Bundle = Bundle()): UiDevice {
        require(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            "HiddenApiBypass needs API 28 or higher."
        }
        initialize()
        InstrumentationRegistry.registerInstance(instrumentation, arguments)
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
}