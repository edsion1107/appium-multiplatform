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
    get() :Int = AndroidVersion.VersionCodes.LOLLIPOP
val Project.compileSdk
    get() :Int = AndroidVersion.VersionCodes.BAKLAVA

val Project.buildTime: Instant by lazy { Instant.now() }
