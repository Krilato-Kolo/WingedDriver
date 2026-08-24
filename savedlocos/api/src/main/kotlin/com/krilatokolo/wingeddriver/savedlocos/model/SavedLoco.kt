package com.krilatokolo.wingeddriver.savedlocos.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavedLoco(
   val address: Int,
   val functions: List<Function> = emptyList(),
   @Json(name = "id")
   val backendId: String? = null,
   val imageUrl: String? = null,
   val name: String = "",
   val normalizedName: String = "",
) {
   @JsonClass(generateAdapter = true)
   data class Function(
      val functionNumber: Int,
      val functionName: String,
      val functionImage: String? = null,
   )
}
