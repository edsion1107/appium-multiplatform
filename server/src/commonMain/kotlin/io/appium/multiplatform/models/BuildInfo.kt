package io.appium.multiplatform.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant


@Serializable
data class BuildInfo(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int,
    val extra: ExtraInfo
)

expect val buildInfo: BuildInfo

@Serializable
sealed class ExtraInfo()

@Serializable
data class AndroidExtraInfo(
    val isDebug: Boolean,
    val buildTime: Instant
) : ExtraInfo()