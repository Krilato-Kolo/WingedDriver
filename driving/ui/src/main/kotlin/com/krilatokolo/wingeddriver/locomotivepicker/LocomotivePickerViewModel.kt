package com.krilatokolo.wingeddriver.locomotivepicker

import androidx.compose.runtime.Stable
import com.krilatokolo.wingeddriver.driving.DrivingController
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import si.inova.kotlinova.core.flow.collectInto
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class LocomotivePickerViewModel(
   private val resources: CoroutineResourceManager,
   private val drivingController: DrivingController,
) : SingleScreenViewModel<LocomotivePickerScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<List<Int>>(emptyList())
   val uiState: StateFlow<List<Int>>
      get() = _uiState

   override fun onServiceRegistered() {
      resources.launchWithExceptionReporting {
         drivingController.locos.collectInto(_uiState)
      }
   }

   fun selectLoco(loco: Int) {
      drivingController.changeLoco(loco)
   }
}
