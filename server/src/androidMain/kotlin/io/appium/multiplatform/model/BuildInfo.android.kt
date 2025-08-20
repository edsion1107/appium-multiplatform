package io.appium.multiplatform.model


import android.os.Build
import com.appium.multiplatform.server.BuildConfig
import kotlin.time.Instant

/**
 * server自身属性
 */
actual val buildInfo: BuildInfo by lazy {
    BuildInfo(
        applicationId = BuildConfig.APPLICATION_ID,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        isDebug = BuildConfig.DEBUG,
        extra = AndroidExtraInfo(isDebug = BuildConfig.DEBUG, buildTime = Instant.parse(BuildConfig.BUILD_TIME))
    )
}
val platform: PlatformInfo by lazy {
    PlatformInfo(arch = Build.SUPPORTED_ABIS.joinToString(","), name = "Android", version = Build.VERSION.RELEASE)
}
actual val state: State
    get() = State(build = buildInfo, platform = platform, ready = false)