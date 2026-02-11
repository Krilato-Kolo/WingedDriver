package com.krilatokolo.wingeddriver.driving

import androidx.compose.runtime.Stable
import com.krilatokolo.wingeddriver.common.ActivityStartedRepository
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
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
) : SingleScreenViewModel<DrivingScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<DrivingState>(DrivingState())
   val uiState: StateFlow<DrivingState>
      get() = _uiState

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
         val flow = combine(drivingController.trackState, drivingController.activeLoco) { trackState, activeLoco ->
            if (activeLoco != null) {
               DrivingState(
                  activeLoco.id,
                  activeLoco.speed,
                  activeLoco.maxSpeed,
                  activeLoco.forward,
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

   fun setSpeed(newSpeed: Int) {
      drivingController.changeSpeed(newSpeed)
   }

   fun setDirection(forward: Boolean) {
      drivingController.changeDirection(forward)
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
}

data class DrivingState(
   val activeLoco: Int? = null,
   val speed: Int = 0,
   val maxSpeed: Int = 0,
   val forward: Boolean = true,
   val connected: Boolean = false,
   val trackPoweredOn: Boolean = false,
   val activeFunctions: List<Int> = emptyList(),
)
