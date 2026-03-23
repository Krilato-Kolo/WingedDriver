package com.krilatokolo.stationmanager.phoneconnection

import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

@Serializable
data class TrainSchedule(val name: String, val stops: List<Stop>)

@Serializable
data class Stop(val name: String, val inbound: Boolean, val time: LocalTime)
