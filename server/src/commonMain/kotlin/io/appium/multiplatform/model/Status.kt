package io.appium.multiplatform.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
sealed class ExtraInfo()

@Serializable
data class AndroidExtraInfo(
    val isDebug: Boolean,
    val buildTime: Instant
) : ExtraInfo()

@Serializable
data class JvmExtraInfo(val buildTime: Instant) : ExtraInfo()

@Serializable
data class BuildInfo(
    val applicationId: String,
    val versionName: String,
    val versionCode: Int,
    val isDebug: Boolean,
    val extra: ExtraInfo
)

@Serializable
data class PlatformInfo(
    val arch: String,
    val name: String,
    val version: String
)

@Serializable
data class Status(
    val build: BuildInfo,
    val platform: PlatformInfo,
    val ready: Boolean = false,
)

