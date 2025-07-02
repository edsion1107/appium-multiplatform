import com.android.adblib.AdbSession
import com.android.adblib.AdbSessionHost
import com.android.adblib.DeviceSelector
import com.android.adblib.WaitForState
import kotlinx.coroutines.runBlocking


fun main(){
    AdbSessionHost().use { host ->
        AdbSession.create(host).use { session ->
            runBlocking {
//                println(session.hostServices.kill())
                println(session.hostServices.serverStatus())
                println(session.hostServices.hostFeatures())
                println(session.hostServices.waitFor(DeviceSelector.any(), WaitForState.ONLINE))
            }
        }
    }
}