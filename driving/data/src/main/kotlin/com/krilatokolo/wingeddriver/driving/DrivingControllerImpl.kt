package com.krilatokolo.wingeddriver.driving

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.krilatokolo.wingeddriver.wifi.LocalWifiConnection
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dispatch.core.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import z21Drive.Z21
import z21Drive.actions.Z21ActionSetLocoDrive
import z21Drive.broadcasts.BroadcastFlagHandler
import z21Drive.broadcasts.BroadcastFlags
import java.net.Inet4Address
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesBinding(AppScope::class)
class DrivingControllerImpl(
   private val localWifiConnection: LocalWifiConnection,
   private val context: Context,
   private val parentScope: DefaultCoroutineScope,
) : DrivingController {
   private val z21 = Z21.instance
   private var connectionScope: CoroutineScope? = null

   override suspend fun connect() {
      println("connect")
      delay(1.seconds)
      val network = localWifiConnection.getCurrentConnection().first()
      val linkProperties = network?.let {
         context.getSystemService<ConnectivityManager>()!!
            .getLinkProperties(it)
      }
      val ip = linkProperties?.linkAddresses?.first { it.address is Inet4Address }?.address

      println("network $network")
      z21.start(network, ip)
      println("started")
      BroadcastFlagHandler.setReceive(BroadcastFlags.GLOBAL_BROADCASTS, true)
      println("set receive success")
      connectionScope = CoroutineScope(parentScope.coroutineContext + SupervisorJob())
   }

   override fun disconnect() {
      // keepaliveJob?.cancel()
      connectionScope?.cancel()
      z21.shutdown()
   }

   @Suppress("MagicNumber") // TMP
   override fun changeSpeed(newSpeed: Int) {
      println("CSA $newSpeed $connectionScope")
      connectionScope?.launch {
         println("SendAction $newSpeed $connectionScope")
         z21.sendActionToZ21(
            Z21ActionSetLocoDrive(
               17,
               newSpeed,
               3,
               true
            )
         )
      }
   }
}
