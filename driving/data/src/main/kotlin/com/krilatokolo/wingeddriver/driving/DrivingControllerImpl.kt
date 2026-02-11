package com.krilatokolo.wingeddriver.driving

import android.content.Context
import android.net.ConnectivityManager
import androidx.core.content.getSystemService
import com.krilatokolo.wingeddriver.wifi.LocalWifiConnection
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dispatch.core.DefaultCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import z21Drive.Z21
import z21Drive.actions.Z21ActionGetLocoInfo
import z21Drive.actions.Z21ActionGetSerialNumber
import z21Drive.actions.Z21ActionLanXTrackPowerOff
import z21Drive.actions.Z21ActionLanXTrackPowerOn
import z21Drive.actions.Z21ActionSetLocoDrive
import z21Drive.actions.Z21ActionSetLocoFunction
import z21Drive.broadcasts.BroadcastFlagHandler
import z21Drive.broadcasts.BroadcastFlags
import z21Drive.broadcasts.BroadcastTypes
import z21Drive.broadcasts.Z21Broadcast
import z21Drive.broadcasts.Z21BroadcastLanXLocoInfo
import z21Drive.broadcasts.Z21BroadcastLanXTrackPowerOff
import z21Drive.broadcasts.Z21BroadcastLanXTrackPowerOn
import z21Drive.broadcasts.Z21BroadcastListener
import java.net.Inet4Address
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DrivingControllerImpl(
   private val localWifiConnection: LocalWifiConnection,
   private val context: Context,
   private val parentScope: DefaultCoroutineScope,
) : DrivingController {
   private val z21 = Z21.instance

   override val locos = MutableStateFlow<List<Int>>(emptyList())
   override val activeLoco = MutableStateFlow<ActiveLocoState?>(null)
   override val trackState = MutableStateFlow<TrackState>(TrackState(false, false))
   private var connectionScope: CoroutineScope? = null

   // TODO how to know when I am actually connected?

   override suspend fun connect() {
      delay(1.seconds)
      val network = localWifiConnection.getCurrentConnection().first()
      val linkProperties = network?.let {
         context.getSystemService<ConnectivityManager>()!!
            .getLinkProperties(it)
      }
      val ip = linkProperties?.linkAddresses?.first { it.address is Inet4Address }?.address

      z21.start(network, ip)
      BroadcastFlagHandler.setReceive(BroadcastFlags.GLOBAL_BROADCASTS, true)
      connectionScope = CoroutineScope(parentScope.coroutineContext + SupervisorJob())

      onConnected()
   }

   override fun disconnect() {
      connectionScope?.launch {
         connectionScope?.cancel()
         z21.shutdown()
         trackState.update { it.copy(connected = false) }
      }
   }

   @Suppress("MagicNumber") // TMP
   override fun changeSpeed(newSpeed: Int) {
      activeLoco.update { it?.copy(speed = newSpeed.coerceIn(0..it.maxSpeed)) }
   }

   override fun changeDirection(forward: Boolean) {
      activeLoco.update { it?.copy(forward = forward) }
   }

   override fun changeLoco(id: Int) {
      activeLoco.value = ActiveLocoState(id, 0, true, 0)

      connectionScope?.launch {
         z21.sendActionToZ21(Z21ActionGetLocoInfo(id))
      }
   }

   override fun toggleTrackPower(poweredOn: Boolean) {
      connectionScope?.launch {
         if (poweredOn) {
            z21.sendActionToZ21(Z21ActionLanXTrackPowerOn())
         } else {
            z21.sendActionToZ21(Z21ActionLanXTrackPowerOff())
         }
      }
   }

   override fun toggleLocoFunction(function: Int, on: Boolean) {
      connectionScope?.launch {
         val activeLocoId = activeLoco.value?.id ?: return@launch
         z21.sendActionToZ21(Z21ActionSetLocoFunction(activeLocoId, function, on))
      }
   }

   val z21broadcastReceiver = object : Z21BroadcastListener {
      override fun onBroadCast(
         type: BroadcastTypes,
         broadcast: Z21Broadcast,
      ) {
         if (!trackState.value.connected) {
            // onConnected()
         }

         when (broadcast) {
            is Z21BroadcastLanXLocoInfo -> {
               locos.update { list ->
                  if (!list.contains(broadcast.locoAddress)) {
                     (list + broadcast.locoAddress).sorted()
                  } else {
                     list
                  }
               }

               activeLoco.update { activeLoco ->
                  if (activeLoco?.id == broadcast.locoAddress) {
                     val functionsAsArray = broadcast.functionsAsArray
                     activeLoco.copy(
                        speed = broadcast.speed,
                        maxSpeed = broadcast.speedSteps - 1,
                        forward = broadcast.direction,
                        activeFunctions = functionsAsArray.indices.filter { functionsAsArray[it] }
                     )
                  } else {
                     activeLoco
                  }
               }
            }

            is Z21BroadcastLanXTrackPowerOn -> {
               trackState.update { it.copy(powerOn = true) }
            }

            is Z21BroadcastLanXTrackPowerOff -> {
               trackState.update { it.copy(powerOn = false) }
            }
         }
      }

      override fun getListenerTypes(): Array<out BroadcastTypes?> {
         return BroadcastTypes.entries.toTypedArray()
      }
   }

   private fun onConnected() {
      trackState.update { it.copy(connected = true) }

      // Keepalive job
      connectionScope?.launch() {
         BroadcastFlagHandler.setReceive(BroadcastFlags.GLOBAL_BROADCASTS, true)

         while (isActive) {
            z21.sendActionToZ21(Z21ActionGetSerialNumber())

            delay(30.seconds)
         }
      }

      // Updater job
      @Suppress("MagicNumber") // Network protocol
      connectionScope?.launch() {
         var lastSpeed = 0
         var lastDirection = true
         while (isActive) {
            activeLoco.value?.let { activeLoco ->
               if (lastSpeed != activeLoco.speed || lastDirection != activeLoco.forward) {
                  z21.sendActionToZ21(
                     Z21ActionSetLocoDrive(
                        activeLoco.id,
                        activeLoco.speed,
                        when (activeLoco.maxSpeed) {
                           13 -> 0
                           27 -> 2
                           else -> 3
                        },
                        activeLoco.forward,
                     )
                  )

                  lastSpeed = activeLoco.speed
                  lastDirection = activeLoco.forward
               }
            }
            delay(150)
         }
      }
   }

   init {
      z21.addBroadcastListener(z21broadcastReceiver)
   }
}
