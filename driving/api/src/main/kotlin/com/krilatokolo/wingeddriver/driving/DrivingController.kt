package com.krilatokolo.wingeddriver.driving

import kotlinx.coroutines.flow.Flow

@Suppress("ComplexInterface") // Lots of loco functions
interface DrivingController {
   val locos: Flow<List<Int>>
   val activeLoco: Flow<ActiveLocoState?>
   val trackState: Flow<TrackState>

   suspend fun connect()

   fun disconnect()

   fun changeSpeed(newSpeed: Float)

   fun changeDirection(forward: Boolean)

   fun changeLoco(id: Int, backendId: String? = null)

   fun toggleTrackPower(poweredOn: Boolean)

   fun toggleLocoFunction(function: Int, on: Boolean)

   fun emergencyStop()
}
