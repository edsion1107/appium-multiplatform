package io.appium.multiplatform.server

//import androidx.test.ext.junit.runners.AndroidJUnit4
import android.app.UiAutomation
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import com.google.protobuf.util.JsonFormat
import io.appium.multiplatform.BySelectorKt
import io.appium.multiplatform.bySelector
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
        val by = By.clazz("clazz").pkg("pkg").text("")

//        val activityClass =Class.forName("com.heytap.yoli.maintabact.MainTabActivity")
//        ActivityScenario.launch<Activity>(Intent().apply {
//            setClassName("com.heytap.yoli", "com.heytap.yoli.maintabact.MainTabActivity")
//            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        })
        val selector = io.appium.multiplatform.By.BySelector.newBuilder()
        val clazzSelector = io.appium.multiplatform.By.BySelector.ClassSelector.newBuilder()
        val classNameWithPackage = io.appium.multiplatform.By.BySelector.ClassSelector.ClassNameWithPackage.newBuilder().apply {
            setClassName("android.widget.TextView")
        }.build()
        selector.setClazz(clazzSelector.setFullClassName(classNameWithPackage))
        val classSelector = io.appium.multiplatform.By.BySelector.ClassSelector.newBuilder().apply {
            classNameString = "android.widget.TextView1"
        }.build()
//    selector.clazz = classSelector
        val req = selector.build()

        io.appium.multiplatform.logger.info { req }
        io.appium.multiplatform.logger.info{JsonFormat.printer().print(req)}
//    logger.info{JsonFormat.printer().preservingProtoFieldNames().print(req)}
//    logger.info{JsonFormat.printer().alwaysPrintFieldsWithNoPresence().print(req)}
//    logger.info{JsonFormat.printer().omittingInsignificantWhitespace().print(req)}
        val claSelector = BySelectorKt.classSelector { classNameString="android.widget.TextView" }
        val dslSelector = bySelector {
            clazz = claSelector
            text = BySelectorKt.textSelector { textString = "123" }
        }.toBuilder().build()
        io.appium.multiplatform.logger.info { dslSelector }
        io.appium.multiplatform.logger.info{JsonFormat.printer().print(dslSelector)}
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