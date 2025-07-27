package io.appium.multiplatform.server

//import androidx.test.ext.junit.runners.AndroidJUnit4
import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.After
import org.junit.Before
import org.junit.Test

class TestDemo {
    init {
        System.setProperty("kotlin-logging-to-android-native", "true")
    }

    val logger = KotlinLogging.logger { }
    val uiAutomation: UiAutomation by lazy { InstrumentationRegistry.getInstrumentation().uiAutomation }

    val uiDevice: UiDevice by lazy { UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) }

    @Before
    fun setUp() {
        logger.info { "setup" }
//        val activityClass =Class.forName("com.heytap.yoli.maintabact.MainTabActivity")
//        ActivityScenario.launch<Activity>(Intent().apply {
//            setClassName("com.heytap.yoli", "com.heytap.yoli.maintabact.MainTabActivity")
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        })

    }

    @Test()
    fun runTest() {
        logger.info { "test" }
        uiAutomation.executeShellCommand("ping -c 10 -i 3 127.0.0.1").let {
            it.fileDescriptor
        }
        val uiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        uiDevice.findObject(By.text("Hello World"))
        uiDevice.executeShellCommand("ping -c 10 -i 3 127.0.0.1")
        Thread.sleep(20_000)
    }

    @After
    fun tearDown() {
        logger.info { "tearDown" }
    }
}