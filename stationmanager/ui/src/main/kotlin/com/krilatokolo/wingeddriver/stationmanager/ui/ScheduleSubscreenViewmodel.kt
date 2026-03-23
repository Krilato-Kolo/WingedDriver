package com.krilatokolo.wingeddriver.stationmanager.ui

import androidx.compose.runtime.Stable
import com.krilatokolo.stationmanager.phoneconnection.MobileConnection
import com.krilatokolo.stationmanager.phoneconnection.TrainSchedule
import com.krilatokolo.wingeddriver.navigation.ScheduleSubscreenKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class ScheduleSubscreenViewmodel(
   private val resources: CoroutineResourceManager,
   private val mobileConnection: MobileConnection,
) : SingleScreenViewModel<ScheduleSubscreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<List<TrainSchedule>>>(Outcome.Progress())
   val uiState: StateFlow<Outcome<List<TrainSchedule>>> = _uiState

   override fun onServiceRegistered() {
      resources.launchResourceControlTask(_uiState) {
         emitAll(mobileConnection.getTrainSchedule().map { Outcome.Success(it) })
      }
   }
}
