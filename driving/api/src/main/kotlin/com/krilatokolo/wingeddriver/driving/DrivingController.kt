package com.krilatokolo.wingeddriver.driving

interface DrivingController {
   suspend fun connect()

   fun disconnect()

   fun changeSpeed(newSpeed: Int)
}
