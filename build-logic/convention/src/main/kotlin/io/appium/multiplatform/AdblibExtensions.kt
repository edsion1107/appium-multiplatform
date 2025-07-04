package io.appium.multiplatform

import com.android.adblib.*
import org.gradle.api.GradleException

suspend fun AdbSession.selectOnlineDevice(serial: String?): ConnectedDevice {
    return serial
        ?.takeIf { it.isNotBlank() }
        ?.let { connectedDevicesTracker.waitForDevice(it) }
        ?: hostServices
            .devices(AdbHostServices.DeviceInfoFormat.BINARY_PROTO_FORMAT)
            .onEach { host.logger.info { "deviceInfo: $it" } }
            .firstOrNull { it.deviceState == DeviceState.ONLINE }
            ?.let { connectedDevicesTracker.waitForDevice(it.serialNumber) }
        ?: throw GradleException("No online device found")
}