package com.krilatokolo.wingeddriver.wifi

import android.net.Network
import kotlinx.coroutines.flow.Flow

interface LocalWifiConnection {
   fun getCurrentConnection(): Flow<Network?>
   fun connect(ssidPrefix: String, password: String)
}
