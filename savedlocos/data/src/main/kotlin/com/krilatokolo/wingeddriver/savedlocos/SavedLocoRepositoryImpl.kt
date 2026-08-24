package com.krilatokolo.wingeddriver.savedlocos

import com.krilatokolo.wingeddriver.common.normalizer.normalize
import com.krilatokolo.wingeddriver.network.services.BaseServiceFactory
import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import si.inova.kotlinova.core.outcome.Outcome

@ContributesBinding(AppScope::class)
@Inject
class SavedLocoRepositoryImpl(
   private val savedLocoService: SavedLocosService,
   @BaseServiceFactory.BaseUrl
   private val backendUrl: String,
) : SavedLocoRepository {
   private val locoCache = MutableStateFlow<Outcome<List<SavedLoco>>>(Outcome.Progress())

   override fun getSavedLocos(): Flow<Outcome<List<SavedLoco>>> {
      return locoCache.onStart {
         refreshLocos()
      }
   }

   private suspend fun refreshLocos() {
      try {
         locoCache.value = Outcome.Success(
            savedLocoService.getLocos().map { loco ->
               loco.copy(
                  imageUrl = "${backendUrl}trains/${loco.backendId!!}/image",
                  normalizedName = loco.name.lowercase().normalize()
               )
            }
         )
      } catch (e: Exception) {
         @Suppress("PrintStackTrace") // I'm lazy, just print it for now
         e.printStackTrace()
      }
   }
}
