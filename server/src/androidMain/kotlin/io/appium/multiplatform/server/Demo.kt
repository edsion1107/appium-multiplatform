package io.appium.multiplatform.server

import android.annotation.SuppressLint
import android.content.pm.IPackageManager
import io.appium.multiplatform.jvm.ReflectiveMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lsposed.hiddenapibypass.HiddenApiBypass

val logger = KotlinLogging.logger { }
@SuppressLint("NewApi")
fun main(args: Array<String>) {
    args.firstOrNull { it.contains("sleep") }?.let {
        for (i in 1..5) {
            Thread.sleep(3_000)
            println("sleep<$i>")
        }
    } ?: println("no sleep")

    val cl = Class.forName("android.app.AppGlobals")
    println("getPackageManager:start update")
    val getPackageManager = HiddenApiBypass.getDeclaredMethod(cl, "getPackageManager")
    println("getPackageManager:$getPackageManager, ${getPackageManager.invoke(null)}")
    val packageManager: IPackageManager =
        ReflectiveMethod<IPackageManager>("android.app.AppGlobals", "getPackageManager").invoke(null)
//    val packageManager = cl.getDeclaredMethod("getPackageManager").let {
//        it.invoke(null)
//    } as IPackageManager

    logger.info { packageManager.allPackages.joinToString(",") }
    packageManager.getTargetSdkVersion("com.heytap.yoli").let {
        logger.info { "targetSdkVersion:$it" }
    }
    //TODO:参照frameworks/base/cmds/am/src/com/android/commands/am/Instrument.java的run方法，调用AMS的 startInstrumentation方法
    //TODO：uiautomator正常编译为apk，但是通过aidl与这里启动的server进行通信
}
