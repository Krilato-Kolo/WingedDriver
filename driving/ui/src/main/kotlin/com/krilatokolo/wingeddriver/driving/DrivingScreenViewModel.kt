package com.krilatokolo.wingeddriver.driving

import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.krilatokolo.wingeddriver.common.ActivityStartedRepository
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.savedlocos.SavedLocoRepository
import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
import com.krilatokolo.wingeddriver.tools.invertDirectionPreference
import com.krilatokolo.wingeddriver.wifi.LocalWifiConnection
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.flow.collectInto
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class DrivingScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val drivingController: DrivingController,
   private val activityStartedRepository: ActivityStartedRepository,
   private val preferenceStore: DataStore<Preferences>,
   private val localWifiConnection: LocalWifiConnection,
   private val savedLocoRepository: SavedLocoRepository,
) : SingleScreenViewModel<DrivingScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<DrivingState>(DrivingState())
   val uiState: StateFlow<DrivingState>
      get() = _uiState

   private var invertDirection: Boolean = false

   override fun onServiceRegistered() {
      resources.launchWithExceptionReporting {
         withDefault {
            activityStartedRepository.activityStarted.collect {
               if (it) {
                  drivingController.connect()
               } else {
                  drivingController.disconnect()
               }
            }
         }
      }

      resources.launchWithExceptionReporting {
         val locoFlow = drivingController.activeLoco.flatMapLatest { activeLoco ->
            val savedLoco = activeLoco?.backendId?.let { id ->
               savedLocoRepository.getLoco(id).map { it.data }
            } ?: flowOf(null)

            savedLoco.map { activeLoco to it }
         }

         val flow = combine(
            preferenceStore.data,
            drivingController.trackState,
            locoFlow,
         ) { preferences, trackState, (activeLoco, savedLoco) ->
            invertDirection = preferences.get(invertDirectionPreference) == true
            if (activeLoco != null) {
               DrivingState(
                  savedLoco ?: SavedLoco(activeLoco.id, createDefaultFunctions(), name = activeLoco.id.toString()),
                  activeLoco.speed,
                  activeLoco.maxSpeed,
                  activeLoco.forward.possiblyInvert(),
                  trackState.connected,
                  trackState.powerOn,
                  activeLoco.activeFunctions,
               )
            } else {
               DrivingState(connected = trackState.connected, trackPoweredOn = trackState.powerOn)
            }
         }

         flow.collectInto(_uiState)
      }
   }

   fun setSpeed(newSpeed: Float) {
      drivingController.changeSpeed(newSpeed)
   }

   fun emergencyStop() {
      drivingController.emergencyStop()
   }

   fun setDirection(forward: Boolean) {
      drivingController.changeDirection(forward.possiblyInvert())
   }

   fun toggleTrackPower(poweredOn: Boolean) {
      drivingController.toggleTrackPower(poweredOn)
   }

   fun toggleLocoFunction(function: Int, on: Boolean) {
      drivingController.toggleLocoFunction(function, on)
   }

   override fun onServiceUnregistered() {
      drivingController.disconnect()
      localWifiConnection.disconnect()
      super.onServiceUnregistered()
   }

   private fun Boolean.possiblyInvert(): Boolean {
      return if (invertDirection) {
         !this
      } else {
         this
      }
   }
}

internal fun createDefaultFunctions(): List<SavedLoco.Function> {
   return List(TOTAL_DEFAULT_LOCO_FUNCTIONS) { index ->
      SavedLoco.Function(index, "F$index")
   }
}

data class DrivingState(
   val activeLoco: SavedLoco? = null,
   val speed: Float = 0f,
   val maxSpeed: Int = 0,
   val forward: Boolean = true,
   val connected: Boolean = false,
   val trackPoweredOn: Boolean = true,
   val activeFunctions: List<Int> = emptyList(),
)

private const val TOTAL_DEFAULT_LOCO_FUNCTIONS = 28
