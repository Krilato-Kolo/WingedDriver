package com.krilatokolo.wingeddriver.ui

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.navigation.keys.WifiConnectScreenKey
import com.krilatokolo.wingeddriver.wifi.LocalWifiConnection
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import si.inova.kotlinova.core.exceptions.UnknownCauseException
import si.inova.kotlinova.core.outcome.CauseException
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.core.outcome.mapData
import si.inova.kotlinova.navigation.instructions.ReplaceBackstack
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.CoroutineScopedService
import kotlin.coroutines.cancellation.CancellationException

@ContributesScopedService
@Inject
class WifiConnectViewModel(
   private val resources: CoroutineResourceManager,
   private val dataStore: DataStore<Preferences>,
   private val localWifiConnection: LocalWifiConnection,
   private val navigator: Navigator,
) : CoroutineScopedService(resources.scope) {
   private val _state = MutableStateFlow<Outcome<WifiConnectScreenModel>>(Outcome.Progress())
   val state: StateFlow<Outcome<WifiConnectScreenModel>> = _state

   override fun onServiceRegistered() {
      resources.launchResourceControlTask(_state) {
         val targetFlow = dataStore.data.map { prefs ->
            Outcome.Success(
               WifiConnectScreenModel(
                  prefs[preferenceSsid].orEmpty(),
                  prefs[preferencePassword].orEmpty(),
               )
            )
         }

         emitAll(targetFlow)
      }

      resources.launchWithExceptionReporting {
         localWifiConnection.getCurrentConnection().collect { conn ->
            if (conn != null) {
               navigator.navigateTo(DrivingScreenKey)
            } else {
               navigator.navigate(ReplaceBackstack(WifiConnectScreenKey))
            }
         }
      }
   }

   fun connect() {
      val state = _state.value.data ?: return

      resources.launchWithExceptionReporting {
         try {
            localWifiConnection.connect(state.ssid, state.password)
         } catch (e: CancellationException) {
            throw e
         } catch (e: CauseException) {
            _state.update { Outcome.Error(e, it.data) }
         } catch (e: Exception) {
            _state.update { Outcome.Error(UnknownCauseException(cause = e), it.data) }
            throw e
         }
      }
   }

   fun setSsid(ssid: String) {
      _state.update { state ->
         state.mapData {
            it.copy(ssid = ssid)
         }
      }

      resources.launchWithExceptionReporting {
         dataStore.edit { preferences ->
            preferences[preferenceSsid] = ssid
         }
      }
   }

   fun setPassword(password: String) {
      _state.update { state ->
         state.mapData {
            it.copy(password = password)
         }
      }

      resources.launchWithExceptionReporting {
         dataStore.edit { preferences ->
            preferences[preferencePassword] = password
         }
      }
   }
}

private val preferenceSsid = stringPreferencesKey("ssid")
private val preferencePassword = stringPreferencesKey("wifipassword")

data class WifiConnectScreenModel(
   val ssid: String,
   val password: String,
)
