package io.appium.multiplatform.server

import android.annotation.SuppressLint
import android.app.Instrumentation
import android.app.UiAutomation
import android.content.Context
import android.os.Build
import android.os.HandlerThread
import android.os.Looper

import androidx.annotation.RequiresApi
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice


object InstrumentShellWrapper : Instrumentation() {
    private const val CONNECT_TIMEOUT_MILLIS = 5000L
    private const val HANDLER_THREAD_NAME = "UiAutomatorHandlerThread"

    private val handlerThread = HandlerThread(HANDLER_THREAD_NAME).apply { start() }
    private val uiAutomationConnection = Class.forName("android.app.UiAutomationConnection")
        .getDeclaredConstructor()
        .apply { isAccessible = true }
        .newInstance()

    private var uiAutomation: UiAutomation? = null

    override fun getContext(): Context? = null

    override fun getTargetContext(): Context? = null

    @SuppressLint("SoonBlockedPrivateApi", "BlockedPrivateApi")
    override fun getUiAutomation(flags: Int): UiAutomation? {
        val automation = uiAutomation
        val uiAutomationClass = Class.forName("android.app.UiAutomation")

        val isDestroyedMethod = uiAutomationClass.getDeclaredMethod("isDestroyed").apply { isAccessible = true }
        val getFlagsMethod = uiAutomationClass.getDeclaredMethod("getFlags").apply { isAccessible = true }
        val connectWithTimeoutMethod = uiAutomationClass.getDeclaredMethod("connect", Int::class.javaPrimitiveType, Long::class.javaPrimitiveType).apply { isAccessible = true }
        val connectMethod = uiAutomationClass.getDeclaredMethod("connect", Int::class.javaPrimitiveType).apply { isAccessible = true }
        val disconnectMethod = uiAutomationClass.getDeclaredMethod("disconnect").apply { isAccessible = true }
        val destroyMethod = uiAutomationClass.getDeclaredMethod("destroy").apply { isAccessible = true }
        val constructor = uiAutomationClass.getDeclaredConstructor(Looper::class.java, Class.forName("android.app.IUiAutomationConnection"))
            .apply { isAccessible = true }

        val mustCreateNew = automation == null || isDestroyedMethod.invoke(automation) == true
        val flagsMatch = automation != null && getFlagsMethod.invoke(automation) == flags

        if (!mustCreateNew && flagsMatch) return automation

        if (mustCreateNew) {
            uiAutomation = constructor.newInstance(handlerThread.looper, uiAutomationConnection) as UiAutomation?
        } else {
            disconnectMethod.invoke(uiAutomation)
        }

        return try {
            if (Build.VERSION.SDK_INT > 30) {
                connectWithTimeoutMethod.invoke(uiAutomation, flags, CONNECT_TIMEOUT_MILLIS)
            } else {
                connectMethod.invoke(uiAutomation, flags)
            }
            connectMethod.invoke(uiAutomation, flags)
            uiAutomation
        } catch (e: Exception) {
            destroyMethod.invoke(uiAutomation)
            uiAutomation = null
            null
        }
    }

    @SuppressLint("SoonBlockedPrivateApi")
    override fun getUiAutomation(): UiAutomation? {
        if (uiAutomation != null) return uiAutomation

        val constructor = Class.forName("android.app.UiAutomation")
            .getDeclaredConstructor(Looper::class.java, Class.forName("android.app.IUiAutomationConnection"))
            .apply { isAccessible = true }

        val connectMethod = Class.forName("android.app.UiAutomation")
            .getDeclaredMethod("connect")
            .apply { isAccessible = true }

        uiAutomation = constructor.newInstance(handlerThread.looper, uiAutomationConnection) as UiAutomation?
        connectMethod.invoke(uiAutomation)
        return uiAutomation
    }

    fun destroy() {
        if (!handlerThread.isAlive) throw IllegalStateException("Already disconnected!")
        handlerThread.quit()
    }
}

@SuppressLint("SoonBlockedPrivateApi")
@RequiresApi(Build.VERSION_CODES.N)
fun main() {
//    Thread {
//        Looper.prepare()
//
//        val connInterface = Class.forName("android.app.IUiAutomationConnection")
//        val proxyConnection = Proxy.newProxyInstance(
//            connInterface.classLoader,
//            arrayOf(connInterface)
//        ) { proxy, method, args ->
//            Log.e("UiAutomationProxy", "call: ${method.name}")
//            null // 或按需返回
//        }
//
//        val uiAutomationClass = Class.forName("android.app.UiAutomation")
//        val constructor = uiAutomationClass.getDeclaredConstructor(
//            Int::class.javaPrimitiveType,
//            Looper::class.java,
//            connInterface
//        ).apply { isAccessible = true }
//
//        val uiAutomation = constructor.newInstance(0, Looper.myLooper(), proxyConnection)
//
//        val instrumentation = Instrumentation()
//        Instrumentation::class.java.getDeclaredField("mUiAutomation").apply {
//            isAccessible = true
//            set(instrumentation, uiAutomation)
//        }
//
//        val uiDevice = UiDevice.getInstance(instrumentation)
//
//        // 示例操作
//        uiDevice.findObject(By.text("信息"))?.click()
//
//        Looper.loop()
//    }.start()
    val uiDevice = UiDevice.getInstance(InstrumentShellWrapper)
    uiDevice.findObject(By.text("信息"))?.click()
}