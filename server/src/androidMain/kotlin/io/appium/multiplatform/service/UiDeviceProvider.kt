package io.appium.multiplatform.service

import android.annotation.SuppressLint
import android.app.IInstrumentationWatcher
import android.app.Instrumentation
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import io.appium.multiplatform.jvm.ReflectiveAccess.Companion.reflectMethod
import io.appium.multiplatform.util.HiddenApi
import io.github.oshai.kotlinlogging.KotlinLogging

@SuppressLint("PrivateApi")
class UiDeviceProvider {
    init {
        @Suppress("DEPRECATION")
        Looper.prepareMainLooper()  // fix: Can't create handler inside thread, that has not called Looper.prepare()
    }
    companion object {
        const val UI_AUTOMATION_CONNECTION_CLASS = "android.app.UiAutomationConnection"
        const val ACTIVITY_THREAD_CLASS = "android.app.ActivityThread"
    }
    private val logger = KotlinLogging.logger {}
    private val componentName =
        ComponentName("com.appium.multiplatform.server", "androidx.test.runner.AndroidJUnitRunner")
    private val instrumentation = Instrumentation() // use `InstrumentationRegistry.getInstrumentation()`

    private val activityThread by lazy {
        HiddenApi.invoke(ACTIVITY_THREAD_CLASS, null, "systemMain")
    }
    private val systemContext by lazy {
        HiddenApi.invoke(ACTIVITY_THREAD_CLASS, activityThread, "getSystemContext") as Context
    }
    private val uiAutomationConnection by lazy {
        HiddenApi.newInstance(UI_AUTOMATION_CONNECTION_CLASS)
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
        val initMethod = reflectMethod(
            Instrumentation::class.java, "init",
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

    fun get(arguments: Bundle = Bundle()): UiDevice {
        initialize()
        InstrumentationRegistry.registerInstance(instrumentation, arguments)
        return UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }
}
