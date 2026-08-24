package com.krilatokolo.wingeddriver.locomotivepicker

import androidx.compose.runtime.Stable
import com.krilatokolo.wingeddriver.common.normalizer.normalize
import com.krilatokolo.wingeddriver.driving.DrivingController
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import com.krilatokolo.wingeddriver.savedlocos.SavedLocoRepository
import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
import dev.zacsweers.metro.Inject
import dispatch.core.flowOnDefault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
   private val savedLocoRepository: SavedLocoRepository,
) : SingleScreenViewModel<LocomotivePickerScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<List<SavedLoco>>(emptyList())
   val uiState: StateFlow<List<SavedLoco>>
      get() = _uiState

   private val filter by savedFlow { "" }

   override fun onServiceRegistered() {
      resources.launchWithExceptionReporting {
         val usedMapped = drivingController.locos.map { list ->
            list.map { SavedLoco(it, name = it.toString(), normalizedName = it.toString()) }
         }
         combine(savedLocoRepository.getSavedLocos(), usedMapped, filter) { savedLocos, usedControllerLocos, filter ->
            val normalFilter = filter.lowercase().normalize()

            (savedLocos.data.orEmpty() + usedControllerLocos).filter {
               it.normalizedName.contains(normalFilter, ignoreCase = true)
            }
         }
            .flowOnDefault()
            .collectInto(_uiState)
      }
   }

   fun setFilter(filter: String) {
      this.filter.value = filter
   }

   fun selectLoco(text: String) {
      val number = text.toIntOrNull()
      val currentLocos = _uiState.value

      if (number != null) {
         drivingController.changeLoco(number)
      } else if (currentLocos.size == 1) {
         drivingController.changeLoco(currentLocos.first().address)
      }
   }
}
