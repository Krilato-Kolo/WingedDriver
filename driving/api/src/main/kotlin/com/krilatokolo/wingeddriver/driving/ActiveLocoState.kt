package com.krilatokolo.wingeddriver.driving

data class ActiveLocoState(
   val id: Int?,
   val speed: Int,
   val forwards: Boolean,
   val maxSpeed: Int?,
)
