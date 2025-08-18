@file:Suppress("UnusedReceiverParameter")

package io.appium.multiplatform.convention


import com.android.sdklib.AndroidVersion
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.time.Instant

// These are LanguageVersion declarations and do not specify concrete artifact dependency versions (i.e., without patch versions).
val Project.javaLanguageVersion
    get() :JavaVersion = JavaVersion.VERSION_21

val Project.kotlinLanguageVersion
    get() :KotlinVersion = KotlinVersion.KOTLIN_2_1

val Project.targetSdk
    get() :Int = AndroidVersion.VersionCodes.BAKLAVA
val Project.minSdk
    // LOLLIPOP (android 5, Level 21) does not support runtime permissions, the installation parameters are inconsistent;
    // uiautomator-shell-android supports at least 23.
    // netty-handler-4.2.2.Final.jar suggest the minSdkVersion to 26 or above,but not work.
    // HiddenApiBypass stable on Android 10+
    // Perfetto is easier to run on Android 11 and above.
    get() :Int = AndroidVersion.VersionCodes.P
val Project.compileSdk
    get() :Int = AndroidVersion.VersionCodes.BAKLAVA

val Project.buildTime: Instant by lazy { Instant.now() }
