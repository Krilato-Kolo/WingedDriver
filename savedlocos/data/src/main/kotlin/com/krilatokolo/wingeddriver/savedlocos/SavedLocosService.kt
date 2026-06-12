package com.krilatokolo.wingeddriver.savedlocos

import com.krilatokolo.wingeddriver.savedlocos.model.SavedLoco
import retrofit2.http.GET

interface SavedLocosService {
   @GET("/trains")
   suspend fun getLocos(): List<SavedLoco>
}
