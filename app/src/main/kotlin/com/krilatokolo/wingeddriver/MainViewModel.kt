package com.krilatokolo.wingeddriver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krilatokolo.wingeddriver.navigation.keys.OnboardingScreenKey
import com.krilatokolo.wingeddriver.navigation.keys.WifiConnectScreenKey
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import si.inova.kotlinova.navigation.screenkeys.ScreenKey

@AssistedInject
class MainViewModel(private val context: Context) : ViewModel() {
   private val _startingScreen = MutableStateFlow<ScreenKey?>(null)
   val startingScreen: StateFlow<ScreenKey?> = _startingScreen

   init {
      viewModelScope.launch {
         val hasAllPermissions =
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || hasPermission(Manifest.permission.POST_NOTIFICATIONS))

         _startingScreen.value = if (hasAllPermissions) {
            WifiConnectScreenKey
         } else {
            OnboardingScreenKey
         }
      }
   }

   private fun hasPermission(permission: String): Boolean =
      ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

   @AssistedFactory
   fun interface Factory {
      fun create(): MainViewModel
   }
}
