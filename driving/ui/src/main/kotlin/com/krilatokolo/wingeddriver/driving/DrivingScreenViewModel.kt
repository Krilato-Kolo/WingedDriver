package com.krilatokolo.wingeddriver.driving

import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.krilatokolo.wingeddriver.common.ActivityStartedRepository
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.tools.invertDirectionPreference
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
         val flow = combine(
            preferenceStore.data,
            drivingController.trackState,
            drivingController.activeLoco
         ) { preferences, trackState, activeLoco ->
            invertDirection = preferences.get(invertDirectionPreference) == true
            if (activeLoco != null) {
               DrivingState(
                  activeLoco.id,
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

data class DrivingState(
   val activeLoco: Int? = null,
   val speed: Float = 0f,
   val maxSpeed: Int = 0,
   val forward: Boolean = true,
   val connected: Boolean = false,
   val trackPoweredOn: Boolean = true,
   val activeFunctions: List<Int> = emptyList(),
)
