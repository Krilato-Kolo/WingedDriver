package com.krilatokolo.wingeddriver.tools.ui

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Stable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.krilatokolo.wingeddriver.common.logging.ActionLogger
import com.krilatokolo.wingeddriver.navigation.keys.ToolsScreenKey
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import si.inova.kotlinova.core.outcome.CoroutineResourceManager
import si.inova.kotlinova.core.outcome.Outcome
import si.inova.kotlinova.navigation.services.ContributesScopedService
import si.inova.kotlinova.navigation.services.SingleScreenViewModel

@Stable
@Inject
@ContributesScopedService
class ToolsViewModel(
   private val resources: CoroutineResourceManager,
   private val actionLogger: ActionLogger,
   private val context: Context,
   private val preferenceStore: DataStore<Preferences>,
) : SingleScreenViewModel<ToolsScreenKey>(resources.scope) {
   private val _uiState = MutableStateFlow<Outcome<ToolsState>>(Outcome.Progress())
   val appVersion: StateFlow<Outcome<ToolsState>>
      get() = _uiState

   private val _logSave = MutableStateFlow<Outcome<Uri?>>(Outcome.Success(null))
   val logSave: StateFlow<Outcome<Uri?>> = _logSave

   override fun onServiceRegistered() {
      actionLogger.logAction { "ToolsViewModel.onServiceRegistered()" }

      val pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
      val versionName = pInfo.versionName.orEmpty()

      resources.launchResourceControlTask(_uiState) {
         emitAll(
            preferenceStore.data.map { preferences ->
               Outcome.Success(
                  ToolsState(
                     versionName,
                     preferences
                  )
               )
            }
         )
      }
   }

   fun resetLog() {
      actionLogger.logAction { "ToolsViewModel.resetLog()" }
      _logSave.value = Outcome.Success(null)
   }

   fun <T> updatePreference(key: Preferences.Key<T>, value: T) = resources.launchWithExceptionReporting {
      actionLogger.logAction { "ToolsViewModel.updatePreference($key)" }
      preferenceStore.edit {
         it[key] = value
      }
   }
}

data class ToolsState(
   val versionName: String,
   val preferences: Preferences,
)

private const val ZIP_BUFFER_SIZE = 1024
