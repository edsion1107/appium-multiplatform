package io.appium.multiplatform.server

//import androidx.annotation.RequiresApi
//import androidx.test.platform.app.InstrumentationRegistry
//import androidx.test.uiautomator.By
//import androidx.test.uiautomator.UiDevice
import android.annotation.SuppressLint
import android.app.Instrumentation
import android.content.Context
import android.content.pm.IPackageManager
import android.os.Looper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lsposed.hiddenapibypass.HiddenApiBypass

private val logger = KotlinLogging.logger {}

/**
 * 封装对Android隐藏系统API的访问。
 * 这里的反射调用是明确和具体的，而不是通用的，以确保稳定性和健壮性。
 */
internal object SystemApis {
    private val activityThreadClass by lazy { Class.forName("android.app.ActivityThread") }
    private val uiAutomationClass by lazy { Class.forName("android.app.UiAutomation") }
    private val iUiAutomationConnectionClass by lazy { Class.forName("android.app.IUiAutomationConnection") }

    val systemContext: Context by lazy {
        logger.debug { "正在获取系统上下文 (system context)..." }
        val currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread")
        val currentActivityThread = currentActivityThreadMethod.invoke(null)
        val getSystemContextMethod = activityThreadClass.getMethod("getSystemContext")
        val context = getSystemContextMethod.invoke(currentActivityThread) as Context
        logger.debug { "系统上下文获取成功。" }
        context
    }

    val uiAutomationConnection: Any by lazy {
        logger.debug { "正在创建 UiAutomationConnection..." }
        val connectionClass = Class.forName("android.app.UiAutomationConnection")
        val constructor = connectionClass.getConstructor()
        constructor.isAccessible = true
        val connection = constructor.newInstance()
        logger.debug { "UiAutomationConnection 创建成功。" }
        connection
    }

    fun createUiAutomation(looper: Looper, connection: Any): Any {
        logger.debug { "正在创建 UiAutomation 实例..." }
        val constructor = uiAutomationClass.getConstructor(Looper::class.java, iUiAutomationConnectionClass)
        constructor.isAccessible = true
        val automation = constructor.newInstance(looper, connection)
        logger.debug { "UiAutomation 实例创建成功。" }
        return automation
    }

    fun connectUiAutomation(automation: Any) {
        logger.debug { "正在连接 UiAutomation 到系统服务..." }
        try {
            when {
                // Android 12 (API 31) 及以上版本，connect 方法需要一个超时参数
//                android.os.Build.VERSION.SDK_INT >= 31 -> {
//                    val connectMethod = uiAutomationClass.getMethod("connect", Int::class.javaPrimitiveType, Long::class.javaPrimitiveType)
//                    // FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES = 1
//                    connectMethod.invoke(automation, 1, 5000L)
//                }
                // Android 11 (API 30) 版本，connect 方法需要一个 flags 参数
//                android.os.Build.VERSION.SDK_INT == 30 -> {
                android.os.Build.VERSION.SDK_INT >= 30 -> {
                    val connectMethod = uiAutomationClass.getMethod("connect", Int::class.javaPrimitiveType)
                    connectMethod.invoke(automation, 1) // FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
                }
                // 更早的版本
                else -> {
                    val connectMethod = uiAutomationClass.getMethod("connect")
                    connectMethod.invoke(automation)
                }
            }
            logger.debug { "UiAutomation 连接成功。" }
        } catch (e: Exception) {
            logger.error(e) { "连接 UiAutomation 失败。" }
            throw e
        }
    }
}

/**
 * 自定义的 Instrumentation 类，作为获取 UiAutomation 的桥梁。
 * 这是一个单例对象，使用 Kotlin 的 object 关键字实现。
 */
internal object AutomatorInstrumentation : Instrumentation() {
    private val handlerThread by lazy {
        android.os.HandlerThread("AutomatorCoreHandlerThread").apply {
            start()
            logger.debug { "后台 HandlerThread 已启动。" }
        }
    }

    // 使用 lazy 保证 UiAutomation 实例是单例并且只在第一次使用时才被初始化
    private val uiAutomationInstance: android.app.UiAutomation by lazy {
        logger.debug { "正在初始化 UiAutomation 实例..." }
        val connection = SystemApis.uiAutomationConnection
        val automation = SystemApis.createUiAutomation(handlerThread.looper, connection)
        SystemApis.connectUiAutomation(automation)
        logger.debug { "UiAutomation 实例已准备就绪。" }
        automation as android.app.UiAutomation
    }

    override fun getUiAutomation(): android.app.UiAutomation {
        return uiAutomationInstance
    }

    // UiDevice 内部逻辑也会调用这个重载方法
    override fun getUiAutomation(flags: Int): android.app.UiAutomation {
        // 忽略传入的 flags，总是返回我们自己的单例
        return uiAutomationInstance
    }

    override fun getContext(): Context {
        return SystemApis.systemContext
    }

    override fun getTargetContext(): Context {
        return SystemApis.systemContext
    }
}

@SuppressLint("NewApi")
fun main() {
    logger.info { "strat" }
//    val activityThreadClass = Class.forName("android.app.ActivityThread")
//    HiddenApiBypass.getDeclaredMethods(activityThreadClass).forEach {
//        logger.info { "${it.name},${it.isAccessible},${it.parameters}" }
//    }
    val cl = Class.forName("android.app.AppGlobals")
    logger.info { "getPackageManager:start" }
    val getPackageManager = HiddenApiBypass.getDeclaredMethod(cl, "getPackageManager")
    logger.info { "getPackageManager:$getPackageManager, ${getPackageManager.invoke(null)}" }
    val packageManager = cl.getDeclaredMethod("getPackageManager").let {
        it.invoke(null)
    } as IPackageManager

//    logger.info { "getPackageManager:$packageManager" }
//    val res = packageManager.javaClass.getDeclaredMethod("getAllPackages").invoke(packageManager) as (List<String>)
    logger.info { packageManager.allPackages.joinToString(",") }
    packageManager.getTargetSdkVersion("com.heytap.yoli").let {
        logger.info { "targetSdkVersion:$it" }
    }
    //TODO:参照frameworks/base/cmds/am/src/com/android/commands/am/Instrument.java的run方法，调用AMS的 startInstrumentation方法
    //TODO：uiautomator正常编译为apk，但是通过aidl与这里启动的server进行通信
    val uiAutomationShellWrapper = Class.forName("com.android.uiautomator.core.UiAutomationShellWrapper")
    val obj = HiddenApiBypass.newInstance(uiAutomationShellWrapper)
    val connectMethod = uiAutomationShellWrapper.getDeclaredMethod("connect")
    connectMethod.invoke(obj)
    logger.info { "obj:$obj, $connectMethod" }
}
