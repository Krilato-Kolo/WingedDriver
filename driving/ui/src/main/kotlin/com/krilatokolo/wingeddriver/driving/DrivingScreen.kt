@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.krilatokolo.wingeddriver.driving

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.VerticalSlider
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.krilatokolo.wingeddriver.GamepadListener
import com.krilatokolo.wingeddriver.controller.ControllerPacket
import com.krilatokolo.wingeddriver.driving.ui.R
import com.krilatokolo.wingeddriver.navigation.keys.DrivingScreenKey
import com.krilatokolo.wingeddriver.navigation.keys.base.LocomotivePickerScreenKey
import com.krilatokolo.wingeddriver.ui.debugging.FullScreenPreviews
import com.krilatokolo.wingeddriver.ui.debugging.PreviewTheme
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import si.inova.kotlinova.navigation.instructions.navigateTo
import si.inova.kotlinova.navigation.navigator.Navigator
import si.inova.kotlinova.navigation.screens.InjectNavigationScreen
import si.inova.kotlinova.navigation.screens.Screen
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

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
         viewModel::toggleLocoFunction,
         { navigator.navigateTo(LocomotivePickerScreenKey) },
         viewModel::emergencyStop,
      )
   }
}

@Composable
private fun DrivingScreenContent(
   state: DrivingState,
   setSpeed: (Float) -> Unit,
   setDirection: (Boolean) -> Unit,
   setTrackPower: (Boolean) -> Unit,
   toggleFunction: (Int, Boolean) -> Unit,
   openLocomotivePicker: () -> Unit,
   emergencyStop: () -> Unit,
) {
   BoxWithConstraints {
      if (maxWidth > maxHeight) {
         DrivingContentLandscape(
            state,
            setSpeed,
            setDirection,
            setTrackPower,
            openLocomotivePicker,
            toggleFunction,
            emergencyStop
         )
      } else {
         DrivingContentPortrait(state, setSpeed, setDirection, setTrackPower, openLocomotivePicker, toggleFunction, emergencyStop)
      }
   }
}

@Composable
private fun DrivingContentPortrait(
   state: DrivingState,
   setSpeed: (Float) -> Unit,
   setDirection: (Boolean) -> Unit,
   setTrackPower: (Boolean) -> Unit,
   openLocomotivePicker: () -> Unit,
   toggleFunction: (Int, Boolean) -> Unit,
   emergencyStop: () -> Unit,
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
         Icon(
            painterResource(R.drawable.ic_disconnected),
            stringResource(R.string.disconnected),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.graphicsLayer {
               alpha = if (state.connected) 1f else 0f
            }
         )

         Clock()

         Spacer(Modifier.weight(1f))

         ToggleButton(
            !state.trackPoweredOn,
            onCheckedChange = { setTrackPower(!it) },
            colors = ToggleButtonDefaults.toggleButtonColors(
               checkedContainerColor = MaterialTheme.colorScheme.error
            )
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

      LazyHorizontalGrid(
         GridCells.Adaptive(96.dp),
         Modifier
            .weight(1f)
            .fillMaxWidth(),
         horizontalArrangement = Arrangement.spacedBy(8.dp),
         verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
         locoFunctions(state, toggleFunction)
      }

      val updatedSpeed by rememberUpdatedState(state.speed)

      Jogwheel(
         currentSpeed = { updatedSpeed },
         bumpSpeed = {
            setSpeed(updatedSpeed + it * JOGWHEEL_SENSITIVITY)
         },
         Modifier
            .weight(1f)
            .fillMaxWidth()
      )

      Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
         Button(onClick = { emergencyStop() }, Modifier.weight(1f)) {
            Text(
               stringResource(R.string.stop),
               modifier = Modifier.fillMaxWidth(),
               textAlign = TextAlign.Center
            )
         }

         val buttonText = if (state.forward) "< \uD83D\uDE82" else "\uD83D\uDE82 >"
         Button(onClick = { setDirection(!state.forward) }, Modifier.weight(1f)) {
            Text(
               buttonText,
               Modifier.fillMaxWidth(),
               textAlign = TextAlign.Center
            )
         }
      }

      val maxSpeedAtLeastOne = state.maxSpeed.coerceAtLeast(1)
      Slider(
         state.speed,
         steps = maxSpeedAtLeastOne,
         onValueChange = {
            setSpeed(it)
         },
         modifier = Modifier
            .fillMaxWidth()
            .systemGestureExclusion()
      )
   }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrivingContentLandscape(
   state: DrivingState,
   setSpeed: (Float) -> Unit,
   setDirection: (Boolean) -> Unit,
   setTrackPower: (Boolean) -> Unit,
   openLocomotivePicker: () -> Unit,
   toggleFunction: (Int, Boolean) -> Unit,
   emergencyStop: () -> Unit,
) {
   val updatedState = rememberUpdatedState(state)
   GamepadControl(setSpeed, updatedState::value, setDirection)

   Row(
      Modifier
         .safeDrawingPadding()
         .fillMaxSize()
         .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
   ) {
      Column(
         Modifier
            .width(48.dp)
            .fillMaxHeight(),
         verticalArrangement = Arrangement.spacedBy(16.dp),
         horizontalAlignment = Alignment.CenterHorizontally,
      ) {
         ToggleButton(
            !state.trackPoweredOn,
            onCheckedChange = { setTrackPower(!it) },
            colors = ToggleButtonDefaults.toggleButtonColors(
               checkedContainerColor = MaterialTheme.colorScheme.error
            )
         ) {
            Icon(painterResource(R.drawable.ic_off), stringResource(R.string.track_turned_off))
         }

         Spacer(Modifier.weight(1f))

         if (!state.connected) {
            Icon(
               painterResource(R.drawable.ic_disconnected),
               stringResource(R.string.disconnected),
               tint = MaterialTheme.colorScheme.error
            )
         }
      }

      Button(onClick = openLocomotivePicker) {
         Text(
            state.activeLoco?.toString().orEmpty(),
            Modifier
               .fillMaxHeight()
               .wrapContentHeight(),
            textAlign = TextAlign.Center,
         )
      }

      Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
         Clock(Modifier.padding(bottom = 4.dp))

         LazyVerticalGrid(
            GridCells.Adaptive(96.dp),
            Modifier
               .weight(1f)
               .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
         ) {
            locoFunctions(state, toggleFunction)
         }
      }

      val updatedSpeed by rememberUpdatedState(state.speed)
      Jogwheel(
         currentSpeed = { updatedSpeed },
         bumpSpeed = {
            setSpeed(updatedSpeed + it * JOGWHEEL_SENSITIVITY)
         },
         Modifier
            .weight(1f)
            .fillMaxHeight()
      )

      Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxHeight()) {
         Button(onClick = { emergencyStop() }, Modifier.weight(1f)) {
            Text(
               stringResource(R.string.stop_newlines),
               modifier = Modifier
                  .fillMaxHeight()
                  .wrapContentHeight(),
               textAlign = TextAlign.Center
            )
         }

         val buttonText = if (state.forward) "/\\\n\uD83D\uDE82" else "\uD83D\uDE82\n\\/"
         Button(onClick = { setDirection(!state.forward) }, modifier = Modifier.weight(1f)) {
            Text(
               buttonText,
               Modifier
                  .fillMaxHeight()
                  .wrapContentHeight(),
               textAlign = TextAlign.Center
            )
         }
      }

      val maxSpeedAtLeastOne = state.maxSpeed.coerceAtLeast(1)
      val sliderState = rememberSliderState(
         state.speed,
         steps = maxSpeedAtLeastOne,
      )

      SideEffect {
         sliderState.value = state.speed
      }
      sliderState.onValueChange = {
         setSpeed(it)
      }
      VerticalSlider(
         sliderState,
         modifier = Modifier
            .systemGestureExclusion()
            .fillMaxHeight()
            .padding(end = 16.dp),
         reverseDirection = true
      )
   }
}

private fun LazyGridScope.locoFunctions(
   state: DrivingState,
   toggleFunction: (Int, Boolean) -> Unit,
) {
   items(TOTAL_LOCO_FUNCTIONS, key = { it }) { index ->
      ToggleButton(
         state.activeFunctions.contains(index),
         onCheckedChange = { toggleFunction(index, it) },
         modifier = Modifier
            .wrapContentHeight()
            .size(96.dp),
         contentPadding = PaddingValues.Zero,
      ) {
         Text("F$index", fontSize = 32.sp)
      }
   }
}

@Composable
private fun Jogwheel(currentSpeed: () -> Float, bumpSpeed: (Float) -> Unit, modifier: Modifier = Modifier) {
   val color = MaterialTheme.colorScheme.onSurface

   var rotation by remember { mutableDoubleStateOf(0.0) }

   val vibrator = LocalContext.current.getSystemService<Vibrator>()!!

   fun updateRotation(diff: Double) {
      val newRotation = rotation + diff
      val prevSection = (rotation / CLICK_SENSITIVITY).toInt()
      val newSection = (newRotation / CLICK_SENSITIVITY).toInt()

      if (newSection != prevSection) {
         if (currentSpeed() < ALMOST_ZERO || currentSpeed() > ALMOST_ONE) {
            vibrator.vibrate(VibrationEffect.createOneShot(LONG_VIBRATION_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
         } else {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
         }
      }

      bumpSpeed(-diff.toFloat())

      rotation = newRotation
   }

   @Suppress("MagicNumber")
   Canvas(
      modifier.pointerInput(Unit) {
         awaitEachGesture {
            val firstDown = awaitFirstDown()

            val radius = min(size.width, size.height) / 2
            val minDistanceFromCenterSquared = square(radius * CLICK_OUTER_AREA)
            val centerX = size.width / 2
            val centerY = size.height / 2
            val downPos = firstDown.position
            var lastAngle = Math.toDegrees(atan2((downPos.y - centerY).toDouble(), (downPos.x - centerX).toDouble())) + 180

            outerLoop@ while (true) {
               val event = awaitPointerEvent()

               for (change in event.changes) {
                  val diffX = change.position.y - centerY
                  val diffY = change.position.x - centerX
                  val distFromCenterSquared = square(diffX) + square(diffY)
                  if (!change.pressed || distFromCenterSquared < minDistanceFromCenterSquared) {
                     break@outerLoop
                  }
                  val moveAngle =
                     Math.toDegrees(
                        atan2(
                           diffX.toDouble(),
                           diffY.toDouble()
                        )
                     ) + 180

                  val absoluteDiff = -(moveAngle - lastAngle)
                  val finalDiff = if (absoluteDiff > 180) {
                     absoluteDiff - 360
                  } else if (absoluteDiff < -180) {
                     absoluteDiff + 360
                  } else {
                     absoluteDiff
                  }
                  updateRotation(finalDiff)
                  lastAngle = moveAngle
               }
            }
         }
      }
   ) {
      val radius = size.minDimension / 2
      val spokeStart = (1 - CLICK_OUTER_AREA)
      drawCircle(color, radius, style = Stroke(1.dp.toPx()))

      // Spokes
      for (i in 0 until 359 step 30) {
         val angle = (i + rotation).mod(360.0)
         val xMult = sin(Math.toRadians(angle))
         val yMult = cos(Math.toRadians(angle))

         val start = Offset(
            (xMult * radius * spokeStart + center.x).toFloat(),
            (yMult * radius * spokeStart + center.y).toFloat(),
         )
         val end = Offset(
            (xMult * radius * 0.9 + center.x).toFloat(),
            (yMult * radius * 0.9 + center.y).toFloat(),
         )

         drawLine(color, start, end, 1.dp.toPx())
      }
   }
}

@Composable
private fun GamepadControl(
   setSpeed: (Float) -> Unit,
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
               setSpeed(it * it)
               setDirection(false)
            }
         } else if (triggerActive) {
            triggerActive = false
            if (!aPressed) {
               setSpeed(0f)
            }
         }
      },
      onRightTriggerUpdate = {
         if (it > 0.01f) {
            triggerActive = true
            if (!aPressed) {
               setSpeed(it * it)
               setDirection(true)
            }
         } else if (triggerActive) {
            triggerActive = false
            if (!aPressed) {
               setSpeed(0f)
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
               setSpeed(0f)
            }

            ControllerPacket.PLAY_FLAG -> {
               setSpeed(0f)
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
         setSpeed(0f)
      }
   )
}

@Composable
private fun Clock(modifier: Modifier = Modifier) {
   val dateFlow = remember {
      flow {
         while (currentCoroutineContext().isActive) {
            val now = Date()
            emit(now)
            val timeToNext = SEOCNDS_IN_MC - (now.time % SEOCNDS_IN_MC)
            delay(timeToNext)
         }
      }
   }

   val clock = dateFlow.collectAsStateWithLifecycle(Date()).value

   Text(TIME_FORMAT.format(clock.time), modifier, fontSize = 24.sp)
}

private const val SEOCNDS_IN_MC = 1000
private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss")

private fun square(a: Float): Float = a * a

@FullScreenPreviews
@Composable
private fun DrivingScreenContentPreview() {
   PreviewTheme {
      DrivingScreenContent(
         DrivingState(
            activeLoco = 10,
            speed = 0.3f,
            maxSpeed = 128,
            forward = true,
            connected = true,
         ),
         {},
         {},
         {},
         { _, _ -> },
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
            speed = 0.3f,
            maxSpeed = 128,
            forward = true,
            connected = false
         ),
         {},
         {},
         {},
         { _, _ -> },
         {},
         {},
      )
   }
}

@Preview
@Composable
private fun DrivingTrackUnpoweredPreview() {
   PreviewTheme {
      DrivingScreenContent(
         DrivingState(
            activeLoco = 10,
            speed = 0.3f,
            maxSpeed = 128,
            forward = true,
            connected = true,
            trackPoweredOn = false,
         ),
         {},
         {},
         {},
         { _, _ -> },
         {},
         {},
      )
   }
}

private const val TOTAL_LOCO_FUNCTIONS = 28
private const val JOGWHEEL_SENSITIVITY = 0.002f
private const val ALMOST_ZERO = 0.01f
private const val ALMOST_ONE = 0.99f
private const val CLICK_SENSITIVITY = 30
private const val LONG_VIBRATION_DURATION_MS = 500L
private const val CLICK_OUTER_AREA = 0.4f
