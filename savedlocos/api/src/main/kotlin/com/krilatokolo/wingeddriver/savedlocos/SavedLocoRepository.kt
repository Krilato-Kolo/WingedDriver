package com.krilatokolo.wingeddriver.savedlocos

import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
import kotlinx.coroutines.flow.Flow
import si.inova.kotlinova.core.outcome.Outcome

interface SavedLocoRepository {
   fun getSavedLocos(): Flow<Outcome<List<SavedLoco>>>
}
