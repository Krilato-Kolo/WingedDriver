package com.krilatokolo.wingeddriver.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.krilatokolo.wingeddriver.navigation.keys.WifiConnectScreenKey
import com.krilatokolo.wingeddriver.ui.components.ProgressErrorSuccessScaffold
import si.inova.kotlinova.compose.flow.collectAsStateWithLifecycleAndBlinkingPrevention
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen

@InjectNavigationScreen
class WifiConnectScreen(
   private val viewModel: WifiConnectViewModel,
) : Screen<WifiConnectScreenKey>() {
   @Composable
   override fun Content(key: WifiConnectScreenKey) {
      val stateOutcome = viewModel.state.collectAsStateWithLifecycleAndBlinkingPrevention()
      ProgressErrorSuccessScaffold(stateOutcome::value) { state ->
         Column(
            Modifier
               .safeDrawingPadding()
               .fillMaxSize()
               .verticalScroll(rememberScrollState())
               .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
         ) {
            TextField(
               state.ssid,
               onValueChange = viewModel::setSsid,
               placeholder = { Text("Network Name") },
               modifier = Modifier.fillMaxWidth()
            )
            TextField(
               state.password,
               onValueChange = viewModel::setPassword,
               placeholder = { Text("Network Password") },
               visualTransformation = PasswordVisualTransformation(),
               modifier = Modifier.fillMaxWidth()
            )

            Button(onClick = { viewModel.connect() }) {
               Text("Connect")
            }
         }
      }
   }
}
