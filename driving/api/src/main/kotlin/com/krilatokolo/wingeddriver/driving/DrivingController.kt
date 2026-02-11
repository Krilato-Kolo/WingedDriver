package com.krilatokolo.wingeddriver.driving

import kotlinx.coroutines.flow.Flow

interface DrivingController {
   val locos: Flow<List<Int>>
   val activeLoco: Flow<ActiveLocoState?>
   val connected: Flow<Boolean>

   suspend fun connect()

   fun disconnect()

   fun changeSpeed(newSpeed: Int)

   fun changeDirection(forward: Boolean)

   fun changeLoco(id: Int)
}
