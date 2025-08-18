package io.appium.multiplatform.server

import android.annotation.SuppressLint
import android.content.pm.IPackageManager
import io.appium.multiplatform.jvm.ReflectiveMethod
import io.github.oshai.kotlinlogging.KotlinLogging
import org.lsposed.hiddenapibypass.HiddenApiBypass

val logger = KotlinLogging.logger { }

@SuppressLint("NewApi", "SoonBlockedPrivateApi")
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

    logger.info { packageManager.allPackages.joinToString(",") }
    packageManager.getTargetSdkVersion("com.heytap.yoli").let {
        logger.info { "targetSdkVersion:$it" }
    }
}
