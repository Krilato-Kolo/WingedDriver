@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.krilatokolo.wingeddriver.driving

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.krilatokolo.wingeddriver.GamepadListener
import com.krilatokolo.wingeddriver.controller.ControllerPacket
import com.krilatokolo.wingeddriver.driving.ui.R
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import com.krilatokolo.wingeddriver.ui.debugging.FullScreenPreviews
import com.krilatokolo.wingeddriver.ui.debugging.PreviewTheme
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
   override fun Content(key: DrivingScreenKey) {
      val state = viewModel.uiState.collectAsState().value

      DrivingScreenContent(
         state,
         viewModel::setSpeed,
         viewModel::setDirection,
         viewModel::toggleTrackPower,
         { navigator.navigateTo(LocomotivePickerScreenKey) }
      )
   }
}

@Composable
private fun DrivingScreenContent(
   state: DrivingState,
   setSpeed: (Int) -> Unit,
   setDirection: (Boolean) -> Unit,
   setTrackPower: (Boolean) -> Unit,
   openLocomotivePicker: () -> Unit,
) {
   val updatedState = rememberUpdatedState(state)
   GamepadControl(setSpeed, updatedState::value, setDirection)

   Column(
      Modifier
         .safeDrawingPadding()
         .fillMaxSize()
         .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
   ) {
      Row(
         Modifier
            .height(48.dp)
            .fillMaxWidth(),
         horizontalArrangement = Arrangement.spacedBy(16.dp),
         verticalAlignment = Alignment.CenterVertically,
      ) {
         if (!state.connected) {
            Icon(
               painterResource(R.drawable.ic_disconnected),
               stringResource(R.string.disconnected),
               tint = MaterialTheme.colorScheme.error
            )
         }

         Spacer(Modifier.weight(1f))

         ToggleButton(
            state.trackPoweredOn,
            onCheckedChange = setTrackPower,
         ) {
            Icon(painterResource(R.drawable.ic_off), stringResource(R.string.track_turned_off))
         }
      }

      Button(onClick = openLocomotivePicker) {
         Text(
            state.activeLoco?.toString() ?: stringResource(R.string.select_train),
            Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
         )
      }

      Spacer(Modifier.weight(1f))
      val buttonText = if (state.forward) "< \uD83D\uDE82" else "\uD83D\uDE82 >"
      Button(onClick = { setDirection(!state.forward) }) {
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
            setSpeed((it * state.maxSpeed).roundToInt())
         },
         modifier = Modifier.fillMaxWidth()
      )
   }
}

@Composable
private fun GamepadControl(
   setSpeed: (Int) -> Unit,
   updatedState: () -> DrivingState,
   setDirection: (Boolean) -> Unit,
) {
   var triggerActive by remember { mutableStateOf(false) }
   var aPressed by remember { mutableStateOf(false) }

   GamepadListener(
      onLeftTriggerUpdate = {
         if (it > 0.01f) {
            triggerActive = true
            if (!aPressed) {
               setSpeed((it * it * updatedState().maxSpeed).roundToInt())
               setDirection(false)
            }
         } else if (triggerActive) {
            triggerActive = false
            if (!aPressed) {
               setSpeed(0)
            }
         }
      },
      onRightTriggerUpdate = {
         if (it > 0.01f) {
            triggerActive = true
            if (!aPressed) {
               setSpeed((it * it * updatedState().maxSpeed).roundToInt())
               setDirection(true)
            }
         } else if (triggerActive) {
            triggerActive = false
            if (!aPressed) {
               setSpeed(0)
            }
         }
      },
      onButtonPressed = {
         when (it) {
            ControllerPacket.A_FLAG -> {
               aPressed = true
            }

            ControllerPacket.RB_FLAG -> {
               setSpeed(updatedState().speed + 1)
            }

            ControllerPacket.LB_FLAG -> {
               setSpeed(updatedState().speed - 1)
            }

            ControllerPacket.BACK_FLAG -> {
               setSpeed(0)
            }

            ControllerPacket.PLAY_FLAG -> {
               setSpeed(0)
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
         setSpeed(0)
      }
   )
}

@FullScreenPreviews
@Composable
private fun DrivingScreenContentPreview() {
   PreviewTheme {
      DrivingScreenContent(
         DrivingState(
            activeLoco = 10,
            speed = 300,
            maxSpeed = 128,
            forward = true,
            connected = true
         ),
         {},
         {},
         {},
         {},
      )
   }
}

@Preview
@Composable
private fun DrivingScreenDisconnectedPreview() {
   PreviewTheme {
      DrivingScreenContent(
         DrivingState(
            activeLoco = 10,
            speed = 300,
            maxSpeed = 128,
            forward = true,
            connected = false
         ),
         {},
         {},
         {},
         {},
      )
   }
}
