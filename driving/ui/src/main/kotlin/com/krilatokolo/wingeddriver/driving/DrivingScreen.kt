package com.krilatokolo.wingeddriver.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.krilatokolo.wingeddriver.GamepadListener
import com.krilatokolo.wingeddriver.controller.ControllerPacket
import com.krilatokolo.wingeddriver.driving.ui.R
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import kotlin.math.roundToInt

@InjectNavigationScreen
class DrivingScreen(
   private val viewModel: DrivingScreenViewModel,
   private val navigator: Navigator,
) : Screen<DrivingScreenKey>() {
   @Composable
   @Suppress("CyclomaticComplexMethod")
   override fun Content(key: DrivingScreenKey) {
      Column(
         Modifier
            .safeDrawingPadding()
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
         verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
         val state = viewModel.uiState.collectAsState().value
         val updatedState by rememberUpdatedState(state)

         var triggerActive by remember { mutableStateOf(false) }
         var aPressed by remember { mutableStateOf(false) }

         GamepadListener(
            onLeftTriggerUpdate = {
               if (it > 0.01f) {
                  triggerActive = true
                  if (!aPressed) {
                     viewModel.setSpeed((it * it * updatedState.maxSpeed).roundToInt())
                     viewModel.setDirection(false)
                  }
               } else if (triggerActive) {
                  triggerActive = false
                  if (!aPressed) {
                     viewModel.setSpeed(0)
                  }
               }
            },
            onRightTriggerUpdate = {
               if (it > 0.01f) {
                  triggerActive = true
                  if (!aPressed) {
                     viewModel.setSpeed((it * it * updatedState.maxSpeed).roundToInt())
                     viewModel.setDirection(true)
                  }
               } else if (triggerActive) {
                  triggerActive = false
                  if (!aPressed) {
                     viewModel.setSpeed(0)
                  }
               }
            },
            onButtonPressed = {
               when (it) {
                  ControllerPacket.A_FLAG -> {
                     aPressed = true
                  }

                  ControllerPacket.RB_FLAG -> {
                     viewModel.setSpeed(updatedState.speed + 1)
                  }

                  ControllerPacket.LB_FLAG -> {
                     viewModel.setSpeed(updatedState.speed - 1)
                  }

                  ControllerPacket.BACK_FLAG -> {
                     viewModel.setSpeed(0)
                  }

                  ControllerPacket.PLAY_FLAG -> {
                     viewModel.setSpeed(0)
                  }
               }
            },
            onButtonReleased = {
               when (it) {
                  ControllerPacket.A_FLAG -> {
                     aPressed = false
                  }
               }
            },
            onControllerDisconnected = {
               viewModel.setSpeed(0)
            }
         )

         Button(onClick = { navigator.navigateTo(LocomotivePickerScreenKey) }) {
            Text(
               state.activeLoco?.toString() ?: stringResource(R.string.select_train),
               Modifier.fillMaxWidth(),
               textAlign = TextAlign.Center
            )
         }

         Spacer(Modifier.weight(1f))
         val buttonText = if (state.forward) "< \uD83D\uDE82" else "\uD83D\uDE82 >"
         Button(onClick = { viewModel.setDirection(!state.forward) }) {
            Text(
               buttonText,
               Modifier.fillMaxWidth(),
               textAlign = TextAlign.Center
            )
         }

         val maxSpeedAtLeastOne = state.maxSpeed.coerceAtLeast(1)
         Slider(
            state.speed / maxSpeedAtLeastOne.toFloat(),
            steps = maxSpeedAtLeastOne,
            onValueChange = {
               viewModel.setSpeed((it * state.maxSpeed).roundToInt())
            },
            modifier = Modifier.fillMaxWidth()
         )
      }
   }
}
