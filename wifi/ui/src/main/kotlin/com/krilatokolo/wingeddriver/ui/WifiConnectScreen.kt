package com.krilatokolo.wingeddriver.ui

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.krilatokolo.wingeddriver.navigation.keys.WifiConnectScreenKey
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class WifiConnectScreen : Screen<WifiConnectScreenKey>() {
   @Composable
   override fun Content(key: WifiConnectScreenKey) {
      Text("Hello", Modifier.safeDrawingPadding())
   }
}
