package com.krilatokolo.stationmanager.common

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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.isActive
import kotlinx.rpc.krpc.ktor.client.installKrpc
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import java.io.IOException
import kotlin.time.Duration.Companion.seconds

@Inject
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class ProxyMobileConnection(
   wifi: LocalWifiConnection,
) : MobileConnection {
   private val serviceFlow: Flow<MobileConnection?> = wifi.getCurrentConnection().transformLatest { network ->
      val rpcClient = HttpClient(OkHttp) {
         installKrpc()

         engine {
            config {
               network?.socketFactory?.let { socketFactory(it) }
            }
         }
      }.rpc {
         url("ws://192.168.0.207:8080/phone")

         rpcConfig {
            serialization {
               json()
            }
         }

         timeout {
            connectTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
            socketTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
            requestTimeoutMillis = DEFAULT_NETWORK_TIMEOUT
         }
      }

      while (currentCoroutineContext().isActive) {
         val service = rpcClient.withService<MobileConnection>()
         emit(service)
         awaitCancellation()
      }
   }

   override fun getTrainSchedule(): Flow<List<TrainSchedule>> = flowWithRetry {
      it?.getTrainSchedule() ?: flowOf(emptyList())
   }

   private inline fun <T> flowWithRetry(crossinline block: (MobileConnection?) -> Flow<T>): Flow<T> {
      return serviceFlow.flatMapLatest { service ->
         flow {
            while (currentCoroutineContext().isActive) {
               try {
                  emitAll(block(service))
               } catch (ignored: IOException) {
                  emitAll(block(null))
               }

               delay(5.seconds)
            }
         }
      }
   }
}

private val DEFAULT_NETWORK_TIMEOUT = 10.seconds.inWholeMilliseconds
