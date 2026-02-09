package com.krilatokolo.wingeddriver.wifi

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.PatternMatcher
import androidx.core.content.getSystemService
import com.krilatokolo.wingeddriver.common.exceptions.RawPrintException
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import si.inova.kotlinova.core.reporting.ErrorReporter

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class LocalWifiConnectionImpl(
   private val context: Context,
   private val errorReporter: ErrorReporter,
) : LocalWifiConnection {
   private val currentConnection = MutableStateFlow<Network?>(null)

   override fun getCurrentConnection(): Flow<Network?> {
      return currentConnection
   }

   override fun connect(ssidPrefix: String, password: String) {
      val wifiManager = context.getSystemService<WifiManager>()!!

      if (!wifiManager.isWifiEnabled) {
         throw RawPrintException("Wifi is not enabled")
      }

      // if (!wifiManager.isStaConcurrencyForLocalOnlyConnectionsSupported) {
      //    // throw Exception("Local Wifi connection not supported.")
      // }

      val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
         .setSsidPattern(PatternMatcher(ssidPrefix, PatternMatcher.PATTERN_PREFIX))
         .setWpa2Passphrase(password)
         .build()

      val networkRequest = NetworkRequest.Builder()
         .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
         .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
         .setNetworkSpecifier(wifiNetworkSpecifier)
         .build()

      val connectivityManager = context.getSystemService<ConnectivityManager>()!!

      connectivityManager.requestNetwork(networkRequest, networkCallback)
   }

   private val networkCallback = object : ConnectivityManager.NetworkCallback() {
      override fun onAvailable(network: Network) {
         super.onAvailable(network)

         currentConnection.value = network
         //                _mainState.subscriptionCount.map { it > 0 }.distinctUntilChanged().collect { active ->
         //                    if (active) startConnection() else stopConnection()
         //                }
         // Local-only WiFi is connected
      }

      override fun onUnavailable() {
         currentConnection.value = null
         super.onUnavailable()
         // Failed to connect
      }

      override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
      }

      override fun onCapabilitiesChanged(
         network: Network,
         networkCapabilities: NetworkCapabilities,
      ) {
      }

      override fun onLinkPropertiesChanged(
         network: Network,
         linkProperties: LinkProperties,
      ) {
      }

      override fun onLosing(network: Network, maxMsToLive: Int) {
      }

      override fun onLost(network: Network) {
      }

      override fun onReserved(networkCapabilities: NetworkCapabilities) {
      }
   }
}
