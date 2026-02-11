package com.krilatokolo.wingeddriver.driving

data class ActiveLocoState(
   val id: Int,
   val speed: Int,
   val forward: Boolean,
   val maxSpeed: Int,
   val activeFunctions: List<Int>,
)
