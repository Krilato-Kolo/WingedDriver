package com.krilatokolo.stationmanager.phoneconnection

import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.annotations.Rpc

@Rpc
interface MobileConnection {
   fun getTrainSchedule(): Flow<List<TrainSchedule>>
}
