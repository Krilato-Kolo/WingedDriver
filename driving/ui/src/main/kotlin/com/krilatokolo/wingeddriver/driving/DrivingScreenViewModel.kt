package com.krilatokolo.wingeddriver.driving

import androidx.compose.runtime.Stable
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import dev.zacsweers.metro.Inject
import dispatch.core.withDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.flow.hasActiveSubscribersFlow
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class DrivingScreenViewModel(
   private val resources: CoroutineResourceManager,
   private val drivingController: DrivingController,
) : SingleScreenViewModel<DrivingScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<DrivingState>(DrivingState())
   val uiState: StateFlow<DrivingState>
      get() = _uiState

   override fun onServiceRegistered() {
      resources.launchWithExceptionReporting {
         withDefault {
            _uiState.hasActiveSubscribersFlow().collect {
               if (it) {
                  drivingController.connect()
               } else {
                  drivingController.disconnect()
               }
            }
         }
      }
   }

   fun setSpeed(newSpeed: Int) {
      drivingController.changeSpeed(newSpeed)
   }

   override fun onServiceUnregistered() {
      drivingController.disconnect()
      super.onServiceUnregistered()
   }

   data class DrivingState(val dummy: String = "")
}
