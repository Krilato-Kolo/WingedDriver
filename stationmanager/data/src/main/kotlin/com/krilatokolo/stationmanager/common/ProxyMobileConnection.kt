package com.krilatokolo.stationmanager.common

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krilatokolo.stationmanager.phoneconnection.MobileConnection
import com.krilatokolo.stationmanager.phoneconnection.TrainSchedule
import com.krilatokolo.wingeddriver.wifi.LocalWifiConnection
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.timeout
import io.ktor.client.request.url
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import logcat.logcat
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class ProxyMobileConnection(
   wifi: LocalWifiConnection,
   private val preferences: DataStore<Preferences>,
) : MobileConnection {
   private val serviceFlow: Flow<MobileConnection?> = serverIpFlow().flatMapLatest { serverIp ->
      if (serverIp == null) {
         flowOf<MobileConnection?>(null)
      } else {
         wifi.getCurrentConnection().transformLatest { network ->
            while (currentCoroutineContext().isActive) {
               val rpcClient = HttpClient(OkHttp) {
                  installKrpc()

                  engine {
                     config {
                        network?.socketFactory?.let { socketFactory(it) }
                     }
                  }
               }.rpc {
                  url("ws://$serverIp:27624/phone")

                  rpcConfig {
                     serialization {
                        json()
                     }

                     connector {
                        callTimeout = DEFAULT_NETWORK_TIMEOUT.milliseconds
                        waitTimeout = DEFAULT_NETWORK_TIMEOUT.milliseconds
                        dontWait()
                     }
                  }

                  timeout {
                     connectTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
                     socketTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
                     requestTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
                  }
               }

               try {
                  val service = rpcClient.withService<MobileConnection>()
                  service.ping()

                  logcat { "Stationmanager online!" }

                  emit(service)

                  while (currentCoroutineContext().isActive) {
                     service.ping()
                     delay(10.seconds)
                  }
               } catch (ignored: Exception) {
                  logcat { "Failed to connect to the stationmanager. Retrying..." }
                  delay(10.seconds)
               }
            }
         }
      }.shareIn(GlobalScope, SharingStarted.WhileSubscribed(1_000))
   }

   override fun getTrainSchedule(): Flow<List<TrainSchedule>> = flowWithRetry {
      it?.getTrainSchedule() ?: flowOf(emptyList())
   }

   override suspend fun ping() {
      serviceFlow.first()?.ping()
   }

   private inline fun <T> flowWithRetry(crossinline block: (MobileConnection?) -> Flow<T>): Flow<T> {
      return serviceFlow.flatMapLatest { service ->
         flow {
            try {
               emitAll(block(service))
            } catch (ignored: Exception) {
               // Do nothing, upstream will send us a new service in case of a crash
            }
         }
      }
   }

   private fun serverIpFlow(): Flow<String?> {
      return preferences.data.map { prefs ->
         prefs[preferenceStationManagerAddress]?.trim()?.takeIf {
            IP_REGEX.matches(it)
         }
      }
         .distinctUntilChanged()
   }
}

private val preferenceStationManagerAddress = stringPreferencesKey("stationManagerAddress")

private val IP_REGEX = Regex("^(((?!25?[6-9])[12]\\d|[1-9])?\\d\\.?\\b){4}\$")
private val DEFAULT_NETWORK_TIMEOUT = 1.seconds.inWholeMilliseconds
