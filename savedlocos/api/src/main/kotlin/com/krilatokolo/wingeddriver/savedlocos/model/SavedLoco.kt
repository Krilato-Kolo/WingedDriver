package com.krilatokolo.wingeddriver.savedlocos.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SavedLoco(
   val address: Int,
   val functions: List<Function> = emptyList(),
   val id: String = "",
   val name: String = "",
) {
   @JsonClass(generateAdapter = true)
   data class Function(
      val functionImage: String,
      val functionName: String,
      val functionNumber: Int,
   )
}
