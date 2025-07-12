import com.android.adblib.*
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.util.concurrent.TimeoutException
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

val logger = KotlinLogging.logger { }

/**
 * Selects an online Android device.
 *
 * If a [serial] number is provided, it waits for the device with that specific serial number to become available.
 * If [serial] is null or blank, it searches for the first available online device.
 *
 * @param serial The serial number of the target device. If null, the first online device will be selected.
 * @param timeout The maximum time to wait for the device to be connected.
 * @return The [ConnectedDevice] instance.
 * @throws IllegalStateException if no online device is found or if the timeout is reached.
 */
suspend fun AdbSession.selectOnlineDevice(
    serial: String?,
    timeout: Duration = 10.seconds.toJavaDuration()
): ConnectedDevice {
    return serial
        ?.takeIf { it.isNotBlank() }
        ?.let {
            connectedDevicesTracker.waitForDevice(it, timeout)
        }
        ?: hostServices
            .devices(AdbHostServices.DeviceInfoFormat.BINARY_PROTO_FORMAT)
            .onEach { host.logger.info { "deviceInfo: $it" } }
            .firstOrNull { it.deviceState == DeviceState.ONLINE }
            ?.let { connectedDevicesTracker.waitForDevice(it.serialNumber, timeout) }
        ?: throw IllegalStateException("No online device found")
}

suspend fun ConnectedDevicesTracker.waitForDevice(serial: String, timeout: Duration): ConnectedDevice {
    try {
        return session.withErrorTimeout(timeout) {
            waitForDevice(serial)
        }
    } catch (e: TimeoutException) {
        throw IllegalStateException("Could not connect to device: $serial", e)
    }
}

fun main() {
    runBlocking {
        AdbSessionHost().use { host ->
            AdbSession.create(host).use { session ->
                session.selectOnlineDevice(null).let { device ->
                    device.shell.executeAsText("kill $(pidof app_process)").let {
                        logger.info { "stdout: ${it.stdout}" }
                        logger.info { "stderr: ${it.stderr}" }
                        logger.info { "exitCode: ${it.exitCode}" }
                    }
                    device.shell.executeAsText("app_process -cp /data/local/tmp/server-debug-v1.0.0-SNAPSHOT.apk -Dkotlin-logging-to-android-native=true /data/local/tmp --application io.appium.multiplatform.server.DemoKt")
                        .let {
                            logger.info { "stdout: ${it.stdout}" }
                            logger.info { "stderr: ${it.stderr}" }
                            logger.info { "exitCode: ${it.exitCode}" }
                        }
                }
            }
        }
    }
}