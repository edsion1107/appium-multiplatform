package io.appium.multiplatform.model

import kotlin.time.Clock

actual val buildInfo: BuildInfo by lazy {
    BuildInfo(
        applicationId = "com.appium.multiplatform.server",
        versionName = "1.0.0",
        versionCode = 1,
        isDebug = true,
        extra = JvmExtraInfo(buildTime = Clock.System.now())
    )
}
val platformInfo: PlatformInfo by lazy {
    PlatformInfo(
        arch = System.getProperty("os.arch"),
        name = System.getProperty("os.name"),
        version = System.getProperty("os.version")
    )
}
actual val state: State
    get() = State(build = buildInfo, platform = platformInfo, ready = false)