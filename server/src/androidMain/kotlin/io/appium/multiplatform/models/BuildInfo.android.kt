package io.appium.multiplatform.models

import com.appium.multiplatform.BuildConfig
import kotlin.time.Instant


actual val buildInfo: BuildInfo by lazy {
    BuildInfo(
        applicationId = BuildConfig.APPLICATION_ID,
        versionName = BuildConfig.VERSION_NAME,
        versionCode = BuildConfig.VERSION_CODE,
        extra = AndroidExtraInfo(isDebug = BuildConfig.DEBUG, buildTime = Instant.parse(BuildConfig.BUILD_TIME))
    )
}